package com.mytasklistapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mytasklistapp.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private CredentialManager credentialManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        credentialManager = CredentialManager.create(requireContext());

        // UI Components
        View btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
        MaterialSwitch switchReviewerMode = view.findViewById(R.id.switchReviewerMode);
        LinearLayout layoutReviewerFields = view.findViewById(R.id.layoutReviewerFields);
        Button btnReviewerLogin = view.findViewById(R.id.btnReviewerLogin);

        // Google Sign In Logic
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // Play Console Reviewer Flow
        switchReviewerMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutReviewerFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnReviewerLogin.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Reviewer Login Successful", Toast.LENGTH_SHORT).show();
            navigateToTaskList();
        });

        // Check if already logged in
        if (mAuth.getCurrentUser() != null) {
            navigateToTaskList();
        }
    }

    private void navigateToTaskList() {
        Navigation.findNavController(requireView()).navigate(R.id.action_login_to_taskList);
    }

    private void signInWithGoogle() {
        progressBar.setVisibility(View.VISIBLE);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(requireContext(), request, null, executor, new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse result) {
                handleSignInResult(result.getCredential());
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Sign in failed: " + e.getMessage());
                        Toast.makeText(requireContext(), "Sign in failed. Please try again.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void handleSignInResult(Credential credential) {
        if (credential instanceof CustomCredential &&
                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed to parse Google ID Token", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        } else {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Authentication cancelled or failed.", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(requireActivity(), task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        navigateToTaskList();
                    } else {
                        Toast.makeText(requireContext(), "Firebase Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
