package javafx.controller;

import app.Main;
import database.entity.Address;
import database.entity.Contact;
import database.entity.Province;
import database.entity.Trade;
import database.exception.DataTooLongViolationException;
import database.service.OfficeService;
import javafx.AddressGeolocation;
import javafx.CustomMessageBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

@Controller
public class AddContactController implements Initializable {
    private OfficeService officeService;
    private String withoutSpacesAtStartAndAndPattern = "^\\S$|^\\S[\\s\\S]*\\S$";
    private ObservableList<Trade> tradeObservableList = FXCollections.observableArrayList();
    private ObservableList<Province> provinceObservableList = FXCollections.observableArrayList();
    private CustomMessageBox customMessageBox;

    @FXML
    private Label labelHeader, labelName, labelTrade, labelEmail, labelPhone, labelStreet, labelPostalCode,
            labelCity, labelProvince;
    @FXML
    private TextField textFieldName, textFieldEmail, textFieldPhoneNumber, textFieldStreet, textFieldPostalCode,
            textFieldCity;
    @FXML
    private ComboBox<Trade> comboBoxTrade;
    @FXML
    private ComboBox<Province> comboBoxProvince;

    public void setFrameObjects(OfficeService officeService) {
        this.officeService = officeService;

        Trade defaultTrade = new Trade("");
        tradeObservableList.add(defaultTrade);
        tradeObservableList.addAll(officeService.getTrades());
        comboBoxTrade.setItems(tradeObservableList);
        comboBoxTrade.getSelectionModel().select(0);

        Province defaultProvince = new Province("");
        provinceObservableList.add(defaultProvince);
        provinceObservableList.addAll(officeService.getProvinces());
        comboBoxProvince.setItems(provinceObservableList);
        comboBoxProvince.getSelectionModel().select(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Preferences pref = Preferences.userRoot();
        labelHeader.setText(pref.get("header",
                "Inter Art Marcin Rogal, ul. Wiktorowska 34, Wapiennik, 42-120 Miedźno, Polska"));
        customMessageBox = new CustomMessageBox("image/icon.png");

        textFieldName.textProperty().addListener((observable, oldValue, newValue) -> nameLabelTextChange());
        textFieldEmail.textProperty().addListener((observable, oldValue, newValue) -> emailLabelTextChange());
        textFieldPhoneNumber.textProperty().addListener((observable, oldValue, newValue) -> phoneNumberTextChange());
        textFieldCity.textProperty().addListener((observable, oldValue, newValue) -> cityLabelTextChange());
        textFieldStreet.textProperty().addListener((observable, oldValue, newValue) -> streetLabelTextChange());
        textFieldPostalCode.textProperty().addListener((observable, oldValue, newValue) -> postalCodeLabelTextChange());
    }

    @FXML
    void buttonAdd_onAction() {
        if (labelName.getText().equals("Podaj nazwę."))
            customMessageBox.showMessageBox(Alert.AlertType.WARNING, "Ostrzeżenie",
                    "Operacja dodania kontaktu nie powiodła się.",
                    "Powód: pole wymaganej nazwy kontaktu nie zostało uzupełnione.").showAndWait();
        else {
            if (labelName.getText().equals("")
                    && (labelEmail.getText().equals("") || labelEmail.getText().equals("Podaj adres e-mail."))
                    && (labelPhone.getText().equals("") || labelPhone.getText().equals("Podaj nr telefonu."))
                    && (labelStreet.getText().equals("") || labelStreet.getText().equals("Podaj ulicę i nr domu/mieszkania."))
                    && (labelPostalCode.getText().equals("") || labelPostalCode.getText().equals("Podaj kod pocztowy."))
                    && (labelCity.getText().equals("") || labelCity.getText().equals("Podaj miasto."))) {
                Address newAddress = null;
                if (labelStreet.getText().equals("") || labelPostalCode.getText().equals("")
                        || labelCity.getText().equals("") || labelProvince.getText().equals(""))
                    if (labelProvince.getText().equals(""))
                        newAddress = new Address(textFieldStreet.getText(), textFieldCity.getText(),
                                textFieldPostalCode.getText(), comboBoxProvince.getSelectionModel().getSelectedItem());
                    else
                        newAddress = new Address(textFieldStreet.getText(), textFieldCity.getText(),
                                textFieldPostalCode.getText());

                Contact newContact;
                if (newAddress == null) {
                    if (labelTrade.getText().equals(""))
                        newContact = new Contact(textFieldName.getText(), textFieldPhoneNumber.getText(),
                                textFieldEmail.getText(), comboBoxTrade.getSelectionModel().getSelectedItem());
                    else
                        newContact = new Contact(textFieldName.getText(), textFieldPhoneNumber.getText(),
                                textFieldEmail.getText());
                } else {
                    if (labelTrade.getText().equals(""))
                        newContact = new Contact(textFieldName.getText(), textFieldPhoneNumber.getText(),
                                textFieldEmail.getText(), comboBoxTrade.getSelectionModel().getSelectedItem(), newAddress);
                    else
                        newContact = new Contact(textFieldName.getText(), textFieldPhoneNumber.getText(),
                                textFieldEmail.getText(), newAddress);
                    AddressGeolocation addressGeolocation = new AddressGeolocation(newContact.getAddress(), provinceObservableList);
                    addressGeolocation.setAddressCoordinates();
                }

                try {
                    officeService.saveContact(newContact);
                    closeFrame();
                } catch (DataTooLongViolationException e) {
                    customMessageBox.showMessageBox(Alert.AlertType.WARNING, "Ostrzeżenie",
                            "Operacja dodania kontaktu nie powiodła się.",
                            "Powód: " + e.getCause().getMessage() + ".").showAndWait();
                }
            } else {
                customMessageBox.showMessageBox(Alert.AlertType.WARNING, "Ostrzeżenie",
                        "Operacja dodania kontaktu nie powiodła się.",
                        "Powód: co najmniej jedna wartość kontaktu ma niepoprawny format lub jest za długa.").showAndWait();
            }
        }
    }

    @FXML
    void buttonCancel_onAction() {
        closeFrame();
    }

    @FXML
    void buttonReset_onAction() {
        textFieldName.setText("");
        comboBoxTrade.getSelectionModel().select(0);
        textFieldEmail.setText("");
        textFieldPhoneNumber.setText("");
        textFieldStreet.setText("");
        textFieldPostalCode.setText("");
        textFieldCity.setText("");
        comboBoxProvince.getSelectionModel().select(0);

        labelTrade.setText("Wybierz branżę.");
        labelProvince.setText("Wybierz województwo.");
    }

    @FXML
    void comboBoxProvince_onAction() {
        if (comboBoxProvince.getSelectionModel().getSelectedItem().getProvince().equals(""))
            labelProvince.setText("Wybierz województwo.");
        else
            labelProvince.setText("");
    }

    @FXML
    void comboBoxTrade_onAction() {
        if (comboBoxTrade.getSelectionModel().getSelectedItem().getTrade().equals(""))
            labelTrade.setText("Wybierz branżę.");
        else
            labelTrade.setText("");
    }

    private void closeFrame() {
        Boolean sceneWasLoadedSuccessfully = true;
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("../../fxml/MainFrame.fxml"));
            loader.load();
        } catch (IOException ioEcx) {
            Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ioEcx);
            sceneWasLoadedSuccessfully = false;
        }

        if (sceneWasLoadedSuccessfully) {
            MainFrameController display = loader.getController();
            display.setFrameObjects(officeService);
            Parent parent = loader.getRoot();
            Stage stage = Main.getMainStage();
            Stage currentStage = (Stage) comboBoxProvince.getScene().getWindow();
            stage.setScene(new Scene(parent, currentStage.getWidth() - 16.0, currentStage.getHeight() - 42.5));
        }
    }

    private void cityLabelTextChange() {
        if (textFieldCity.getText().isEmpty())
            labelCity.setText("Podaj miasto.");
        else if (!textFieldCity.getText().matches(withoutSpacesAtStartAndAndPattern)) {
            labelCity.setText("Niepoprawny format.");
        } else if (textFieldCity.getText().length() > 50)
            labelCity.setText("Przekroczono limit znaków.");
        else
            labelCity.setText("");
    }

    private void emailLabelTextChange() {
        String emailPattern = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\" +
                "x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(" +
                "?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]" +
                "|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01" +
                "-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";
        if (textFieldEmail.getText().isEmpty())
            labelEmail.setText("Podaj adres e-mail.");
        else if (!textFieldEmail.getText().matches(emailPattern)) {
            labelEmail.setText("Niepoprawny format.");
        } else if (textFieldEmail.getText().length() > 45)
            labelEmail.setText("Przekroczono limit znaków.");
        else
            labelEmail.setText("");
    }

    private void nameLabelTextChange() {
        if (textFieldName.getText().isEmpty())
            labelName.setText("Podaj nazwę.");
        else if (!textFieldName.getText().matches(withoutSpacesAtStartAndAndPattern)) {
            labelName.setText("Niepoprawny format.");
        } else if (textFieldName.getText().length() > 100)
            labelName.setText("Przekroczono limit znaków.");
        else
            labelName.setText("");
    }

    private void phoneNumberTextChange() {
        String phoneNumberPattern = "^[1-9][0-9]{8}$";
        if (textFieldPhoneNumber.getText().isEmpty())
            labelPhone.setText("Podaj nr telefonu.");
        else if (!textFieldPhoneNumber.getText().matches(phoneNumberPattern))
            labelPhone.setText("Niepoprawny format.");
        else
            labelPhone.setText("");
    }

    private void postalCodeLabelTextChange() {
        String postalCodePattern = "^[0-9]{2}-[0-9]{3}$";
        if (textFieldPostalCode.getText().isEmpty())
            labelPostalCode.setText("Podaj kod pocztowy.");
        else if (!textFieldPostalCode.getText().matches(postalCodePattern))
            labelPostalCode.setText("Niepoprawny format.");
        else
            labelPostalCode.setText("");
    }

    private void streetLabelTextChange() {
        if (textFieldStreet.getText().isEmpty())
            labelStreet.setText("Podaj ulicę i nr domu/mieszkania.");
        else if (!textFieldStreet.getText().matches(withoutSpacesAtStartAndAndPattern)) {
            labelStreet.setText("Niepoprawny format.");
        } else if (textFieldStreet.getText().length() > 50)
            labelStreet.setText("Przekroczono limit znaków.");
        else
            labelStreet.setText("");
    }
}
