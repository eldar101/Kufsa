package com.example.kufsa.ui.game_details;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.kufsa.R;
import com.example.kufsa.data.MarketAd;
import com.example.kufsa.data.MarketAd.TradeType;
import com.example.kufsa.databinding.DialogPublishNewListingBinding;
import com.example.kufsa.utils.Tools;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import org.apache.commons.text.WordUtils;

import java.util.Date;

public class DialogPublishNewListingFragment extends DialogFragment {


    private DialogPublishNewListingBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String gameID;
    CountryCodePicker ccp;// country code picker


    public DialogPublishNewListingFragment() {
        super(R.layout.dialog_publish_new_listing);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DialogPublishNewListingBinding.inflate(inflater, container, false);
        gameID = DialogPublishNewListingFragmentArgs.fromBundle(getArguments()).getGameID();
        ccp = binding.ccp;
        ccp.registerCarrierNumberEditText(binding.phoneText);
        setUIAndListeners();
        return binding.getRoot();
    }

    private void setUIAndListeners() {
        //setting trade type option adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.trade_type_option, android.R.layout.simple_spinner_dropdown_item);
        binding.tradeTypeTextViewSpinner.setAdapter(adapter);
        //setting game condition option adapter
        adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.game_condition_option, android.R.layout.simple_spinner_dropdown_item);
        binding.conditionTextViewSpinner.setAdapter(adapter);
        //setting rent period option adapter
        adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.period_option, android.R.layout.simple_spinner_dropdown_item);
        binding.periodTextViewSpinner.setAdapter(adapter);


        binding.tradeTypeTextViewSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tradeTypeInputLayout.setError(null);
                if (binding.tradeTypeTextViewSpinner.getText().toString().equalsIgnoreCase("Sell")) {
                    binding.sellPriceLayout.setVisibility(View.VISIBLE);
                    binding.rentalSpaceView.setVisibility(View.GONE);
                    binding.rentalPriceLayout.setVisibility(View.GONE);
                }
                if (binding.tradeTypeTextViewSpinner.getText().toString().equalsIgnoreCase("Rent")) {
                    binding.rentalPriceLayout.setVisibility(View.VISIBLE);
                    binding.rentalSpaceView.setVisibility(View.VISIBLE);
                    binding.sellPriceLayout.setVisibility(View.GONE);
                }
                binding.paymentMethodLabel.setVisibility(View.VISIBLE);
                binding.paymentOptionsLayout.setVisibility(View.VISIBLE);

                if (binding.tradeTypeTextViewSpinner.getText().toString().equalsIgnoreCase("Exchange")) {
                    binding.rentalPriceLayout.setVisibility(View.GONE);
                    binding.rentalSpaceView.setVisibility(View.GONE);
                    binding.sellPriceLayout.setVisibility(View.GONE);
                    binding.paymentMethodLabel.setVisibility(View.GONE);
                    binding.paymentOptionsLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (user.getEmail() != null) binding.emailText.setText(user.getEmail());
        if (user.getPhoneNumber() != null) binding.phoneText.setText(user.getPhoneNumber());

        binding.emailCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                binding.emailTextInputLayout.setVisibility(View.VISIBLE);
            else
                binding.emailTextInputLayout.setVisibility(View.GONE);
        });
        binding.phoneCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                binding.phoneInputLayout.setVisibility(View.VISIBLE);

            } else {
                if (!binding.whatsappCheckbox.isChecked())
                    binding.phoneInputLayout.setVisibility(View.GONE);
            }
        });
        binding.whatsappCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                binding.phoneInputLayout.setVisibility(View.VISIBLE);
            else {
                if (!binding.phoneCheckbox.isChecked())
                    binding.phoneInputLayout.setVisibility(View.GONE);
            }
        });


        binding.btClose.setOnClickListener(v -> dismiss());
        binding.btSave.setOnClickListener(v -> {
            // check for errors in form
            if (notValidDataInForm())
                return;


            MarketAd ad = new MarketAd();
            ad.setUserID(user.getUid());
            ad.setUserName(user.getDisplayName());
            ad.setGameID(gameID);
            ad.setTradeType(getTradeType(binding.tradeTypeTextViewSpinner.getText().toString()));
            ad.setCondition(getCondition(binding.conditionTextViewSpinner.getText().toString()));
            ad.setCity(WordUtils.capitalize(binding.cityText.getText().toString()).trim());
            ad.setEmailContact(binding.emailCheckbox.isChecked());
            ad.setPhoneContact(binding.phoneCheckbox.isChecked());
            ad.setWhatsappContact(binding.whatsappCheckbox.isChecked());
            ad.setCash(binding.cashCheckbox.isChecked());
            ad.setCreditCard(binding.creditCardCheckbox.isChecked());
            ad.setBitcoin(binding.bitcoinCheckbox.isChecked());
            // ad.setOther(binding.otherCheckbox.isChecked());
            ad.setNotes(binding.notesText.getText().toString().trim());
            ad.setPublishDate(new Date());
            if (ad.getTradeType() == TradeType.SELL)
                ad.setSellPrice(Double.parseDouble(binding.sellPriceText.getText().toString()));
            if (ad.getTradeType() == TradeType.RENT) {
                ad.setRentalFee(Double.parseDouble(binding.rentalFee.getText().toString()));
                ad.setRentalPeriod(getPeriod(binding.periodTextViewSpinner.getText().toString()));
            }
            ad.setPhone(ccp.getFullNumber());
            ad.setEmail(binding.emailText.getText().toString().trim());

            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
            ad.setUserPhotoUrl(photoUrl);
            db.collection("users").document(user.getUid()).update("photoUrl", photoUrl);
            db.collection("games").document(gameID).collection("listing").add(ad)
                    .addOnSuccessListener(runnable -> {
                        Toast.makeText(requireContext(), "listing saved", Toast.LENGTH_LONG).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismiss();
                    });

        });
    }

    private boolean notValidDataInForm() {

        if (isEmptyEditText(binding.tradeTypeTextViewSpinner, binding.tradeTypeInputLayout) ||
                isEmptyEditText(binding.conditionTextViewSpinner, binding.conditionInputLayout) ||
                isEmptyEditText(binding.cityText, binding.cityInputLayout) ||
                isEmptyEditText(binding.sellPriceText, binding.priceInputLyt) ||
                isEmptyEditText(binding.rentalFee, binding.rentalFeeInputLyt) ||
                isEmptyEditText(binding.periodTextViewSpinner, binding.periodInputLyt) ||
                isEmptyEditText(binding.emailText, binding.emailTextInputLayout) ||
                isEmptyEditText(binding.phoneText, binding.phoneTextInputLayout))
            return true;

        if (Tools.isVisible(binding.emailTextInputLayout) && !Tools.isValidEmail(binding.emailText.getText())) {
            binding.emailTextInputLayout.setError("invalid Email");
            binding.emailText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.emailTextInputLayout.setError(null);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            return true;

        }
        if (Tools.isVisible(binding.phoneTextInputLayout) && !ccp.isValidFullNumber()) {
            binding.phoneTextInputLayout.setError("invalid phone number");
            ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
                if (isValidNumber)
                    binding.phoneTextInputLayout.setError(null);
                else
                    binding.phoneTextInputLayout.setError("invalid phone number");
            });
            return true;
        }

        if (Tools.isVisible(binding.paymentOptionsLayout) && !binding.cashCheckbox.isChecked() && !binding.creditCardCheckbox.isChecked() && !binding.bitcoinCheckbox.isChecked()) {
            Toast.makeText(requireContext(), "Select at least one payment method", Toast.LENGTH_LONG).show();
            return true;
        }
        if (!binding.phoneCheckbox.isChecked() && !binding.emailCheckbox.isChecked() && !binding.whatsappCheckbox.isChecked()) {
            Toast.makeText(requireContext(), "Select at least one contact option", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private boolean isEmptyEditText(EditText editText, TextInputLayout inputLyt) {
        if (Tools.isVisible(inputLyt) && (editText.getText() == null || editText.getText().toString().isEmpty())) {
            inputLyt.setError("required");
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    inputLyt.setError(null);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            return true;
        }
        return false;
    }


    private MarketAd.RentalPeriod getPeriod(String period) {
        switch (period.toLowerCase()) {
            case "per day":
                return MarketAd.RentalPeriod.DAY;
            case "per week":
                return MarketAd.RentalPeriod.WEEK;
            case "per month":
                return MarketAd.RentalPeriod.MONTH;
            default:
                return MarketAd.RentalPeriod.MONTH;

        }
    }

    private MarketAd.Condition getCondition(String condition) {
        switch (condition.trim().toLowerCase()) {
            case "new":
                return MarketAd.Condition.NEW;
            case "like new":
                return MarketAd.Condition.LIKE_NEW;
            case "very good":
                return MarketAd.Condition.VERY_GOOD;
            case "good":
                return MarketAd.Condition.GOOD;
            case "acceptable":
                return MarketAd.Condition.ACCEPTABLE;
            default:
                return MarketAd.Condition.ACCEPTABLE;

        }
    }

    private MarketAd.TradeType getTradeType(String type) {
        switch (type.toLowerCase()) {
            case "sell":
                return TradeType.SELL;
            case "rent":
                return MarketAd.TradeType.RENT;
            case "exchange":
                return MarketAd.TradeType.EXCHANGE;
            default:
                return MarketAd.TradeType.SELL;
        }
    }

}