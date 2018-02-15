package geolocation;

import app.Main;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import controller.GeolocationAddressController;
import database.entity.Address;
import database.entity.Province;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class AddressGeolocation {
    private Preferences pref;
    private Address address;
    private ObservableList<Province> provinceObservableList;

    public AddressGeolocation(Address address, ObservableList<Province> provinceObservableList) {
        pref = Preferences.userRoot();
        this.address = address;
        this.provinceObservableList = provinceObservableList;
    }

    public void setAddressCoordinates() {
        String fullAddress = "";
        if (address.getProvince() != null)
            fullAddress += address.getProvince().getProvince() + ", ";
        if (!address.getPostalCode().equals(""))
            fullAddress += address.getPostalCode() + " ";
        if (!address.getCity().equals(""))
            fullAddress += address.getCity() + ", ";
        if (!address.getStreet().equals(""))
            fullAddress += address.getStreet();
        if (fullAddress.endsWith(" "))
            fullAddress = fullAddress.substring(0, fullAddress.length() - 1);
        if (fullAddress.endsWith(","))
            fullAddress = fullAddress.substring(0, fullAddress.length() - 1);

        try {
            GeoApiContext context = new GeoApiContext.Builder().apiKey(pref.get("google_api_key", "")).build();
            GeocodingResult[] geocodingResults;
            geocodingResults = GeocodingApi.geocode(context, fullAddress).await();
            if (geocodingResults.length >= 1) {
                geolocationAddressFrame(geocodingResults, address);

            } else
                showMessageBox(Alert.AlertType.WARNING,
                        "Powód: proces geolokalizacji Google Maps nie odnalazł żadnego rzeczywistego adresu" +
                                "skojarzonego z wprowadzonymi danymi adresowymi kontaktu.").showAndWait();
        } catch (InterruptedException | IOException | ApiException | IllegalStateException e) {
            switch (e.getMessage()) {
                case "No route to host: connect":
                    showMessageBox(Alert.AlertType.WARNING,
                            "Powód: brak połączenia z Internetem.").showAndWait();
                    break;
                case "The provided API key is invalid.":
                    showMessageBox(Alert.AlertType.WARNING,
                            "Powód: wprowadzony klucz Google Maps API jest niepoprawny.").showAndWait();
                    break;
                case "You have exceeded your daily request quota for this API.":
                    showMessageBox(Alert.AlertType.WARNING,
                            "Powód: przekroczono dzienny limit zapytań dla geolokalizacji Google Maps.").showAndWait();
                    break;
            }
        }
    }

    private void geolocationAddressFrame(GeocodingResult[] geocodingResults, Address address) {
        Boolean sceneWasLoadedSuccessfully = true;
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("../fxml/GeolocationAddress.fxml"));
            loader.load();
        } catch (IOException ioEcx) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ioEcx);
            sceneWasLoadedSuccessfully = false;
        }

        if (sceneWasLoadedSuccessfully) {
            GeolocationAddressController display = loader.getController();
            display.setAddressComponents(geocodingResults, address, provinceObservableList);
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.resizableProperty().setValue(Boolean.FALSE);
            stage.setTitle("Inter Art - Geolokalizacja Google Maps");
            stage.getIcons().add(new Image("/image/icon.png"));
            stage.setScene(new Scene(root, 819, 650));
            stage.showAndWait();
        }
    }

    private Alert showMessageBox(Alert.AlertType alertType, String content) {
        Alert alert = new Alert(alertType);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("image/icon.png"));
        alert.setTitle("Ostrzeżenie");
        alert.setHeaderText("Proces geokodowania nie powiódł się.");
        alert.setContentText(content);
        return alert;
    }
}
