package com.example.kufsa.ui.game_details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kufsa.R;
import com.example.kufsa.data.MarketAd;
import com.example.kufsa.databinding.FragmentGameMarketplaceTabBinding;
import com.example.kufsa.ui.login.LoginFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * This fragment instantiates the game marketplace tab
 */
public class GameMarketplaceTabFragment extends Fragment {

    FragmentGameMarketplaceTabBinding binding;
    String gameID;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    MarketAdapter adapter;


    /**
     * This method initializes the layout for the page from an XML file.
     */
    public GameMarketplaceTabFragment() {
        super(R.layout.fragment_game_marketplace_tab);
    }

    public static void showNoAdsText() {

    }

    /**
     * @param inflater           Instantiates a layout XML file into its corresponding View objects.
     * @param container          special view that can contain child views.
     * @param savedInstanceState A mapping from String keys to various Parcelable values.
     * @return outermost view.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        gameID = getArguments() != null ? getArguments().getString("gameID") : null;
        binding = FragmentGameMarketplaceTabBinding.inflate(inflater, container, false);
        setUpRecyclerView();
        return binding.getRoot();
    }

    private void setUpRecyclerView() {
        Query query =
                db.collection("games").document(gameID).collection("listing").orderBy("publishDate", Query.Direction.DESCENDING);
        query.addSnapshotListener((value, error) -> {
            if (value != null && value.isEmpty()) {
                binding.noAdsLabel.setVisibility(View.VISIBLE);
            }
        });
        // Configure recycler adapter options:
        //  options instructs the adapter to convert each DocumentSnapshot to a BoardGame object
        FirestoreRecyclerOptions<MarketAd> options = new FirestoreRecyclerOptions.Builder<MarketAd>()
                .setQuery(query, MarketAd.class)
                .setLifecycleOwner(this.getViewLifecycleOwner())
                .build();
        adapter = new MarketAdapter(options);

        /*adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int totalNumberOfItems = adapter.getItemCount();
                if(totalNumberOfItems == 0) {
                    binding.noAdsLabel.setVisibility(View.VISIBLE);
                }
            }
        });*/
        binding.recyclerMarket.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerMarket.setAdapter(adapter);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.publishYourGameButton.setOnClickListener(view1 -> {
            if (auth.getCurrentUser() == null) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(LoginFragment.providers)
                                .setLogo(R.drawable.logo)
                                .setTheme(R.style.Theme_purple_firebase)
                                .build(),
                        LoginFragment.RC_SIGN_IN);
                return;
            }
            NavHostFragment.findNavController(this).navigate(GameDetailsTabsContainerFragmentDirections.actionGameDetailsTabsContainerFragmentToDialogPublishNewListingFragment(gameID));
        });
    }

}
