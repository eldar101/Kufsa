package com.example.kufsa.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.kufsa.R;
import com.example.kufsa.databinding.FragmentLoginBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginFragment extends Fragment {

    // Choose an arbitrary request code value
    public static final int RC_SIGN_IN = 123;
    public static final List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.PhoneBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    // creating an auth listener for our Firebase auth
    FirebaseAuth auth;
    String displayName;
    private FragmentLoginBinding binding;
    private FirebaseAuth.AuthStateListener mAuthStateListner;
    private TextView navDisplayName;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            NavHostFragment.findNavController(this).navigate(LoginFragmentDirections.actionMyAccountFragmentToSignedInAccountFragment());
            // already signed in
        }

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();

        setAuthStateListener();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.loginButton.setOnClickListener(view1 -> startActivityForResult(
                // Get an instance of AuthUI based on the default app
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.logo)
                        .setTheme(R.style.Theme_purple_firebase)
                        .build(),
                RC_SIGN_IN));
        setUpDisplayNameInSidebar();
    }

    private void setUpDisplayNameInSidebar() {
        // set up display name in side bar
        NavigationView navView = binding.getRoot().getRootView().findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        navDisplayName = headerView.findViewById(R.id.EmailView);
        if (auth.getCurrentUser() == null)
            displayName = "";
        else
            displayName = auth.getCurrentUser().getDisplayName();
        navDisplayName.setText(displayName);
    }

    private void setAuthStateListener() {
        mAuthStateListner = firebaseAuth -> {


            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // if the user is already authenticated then we will
                // redirect our user to next screen which is our home screen.
                // we are redirecting to new screen via an intent.
                NavHostFragment.findNavController(this).navigate(LoginFragmentDirections.actionMyAccountFragmentToSignedInAccountFragment());
            } else {
                /*// this method is called when our
                // user is not authenticated previously.
                startActivityForResult(
                        // below line is used for getting
                        // our authentication instance.
                        AuthUI.getInstance()
                                // below line is used to
                                // create our sign in intent
                                .createSignInIntentBuilder()

                                // below line is used for adding smart
                                // lock for our authentication.
                                // smart lock is used to check if the user
                                // is authentication through different devices.
                                // currently we are disabling it.
                                // .setIsSmartLockEnabled(false)

                                // we are adding different login providers which
                                // we have mentioned above in our list.
                                // we can add more providers according to our
                                // requirement which are available in firebase.
                                .setAvailableProviders(providers)

                                // below line is for customizing our theme for
                                // login screen and set logo method is used for
                                // adding logo for our login page.
                                .setLogo(R.drawable.logo)

                                // after setting our theme and logo
                                // we are calling a build() method
                                // to build our login screen.
                                .build(),
                        // and lastly we are passing our const
                        // integer which is declared above.
                        RC_SIGN_IN
                );*/
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        // we are calling our auth
        // listener method on app resume.
        auth.addAuthStateListener(mAuthStateListner);
    }

    @Override
    public void onPause() {
        super.onPause();
        // here we are calling remove auth
        // listener method on stop.
        auth.removeAuthStateListener(mAuthStateListner);
    }

}
