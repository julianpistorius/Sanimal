package controller;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.converter.DefaultStringConverter;
import library.EditCell;
import library.TableColumnHeaderUtil;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.cyverse.Permission;
import model.util.FinishableTask;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ImageCollectionSettingsController implements Initializable
{
	///
	/// FXML Bound fields start
	///


	@FXML
	public TextField txtName;
	@FXML
	public TextField txtOrganization;
	@FXML
	public TextField txtContactInfo;
	@FXML
	public TextArea tbxDescription;

	// The actual tableview
	@FXML
	public TableView<Permission> tvwPermissions;
	// All 4 columns
	@FXML
	public TableColumn<Permission, String> clmUser;
	@FXML
	public TableColumn<Permission, Boolean> clmRead;
	@FXML
	public TableColumn<Permission, Boolean> clmUpload;
	@FXML
	public TableColumn<Permission, Boolean> clmOwner;

	// Buttons to remove a user, save permissions, and add a new user
	@FXML
	public Button btnRemoveUser;
	@FXML
	public Button btnSave;
	@FXML
	public Button btnAddUser;
	@FXML
	public Button btnTransferOwnership;

	///
	/// FXML Bound fields end
	///

	// A list of Property<Object> that is used to store weak listeners to avoid early garbage collection. This concept is strange and difficult to
	// understand, here's some articles on it:
	// https://stackoverflow.com/questions/23785816/javafx-beans-binding-suddenly-stops-working
	// https://stackoverflow.com/questions/14558266/clean-javafx-property-listeners-and-bindings-memory-leaks
	// https://stackoverflow.com/questions/26312651/bidirectional-javafx-binding-is-destroyed-by-unrelated-code
	private final List<Property> hardReferences = new ArrayList<>();

	// The currently selected image collection
	private ObjectProperty<ImageCollection> selectedCollection = new SimpleObjectProperty<>();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// Bind the name property of the current collection to the name text property
		// We cache the property so that it does not get garbage collected early
		this.txtName.textProperty().bindBidirectional(cache(EasyBind.monadic(selectedCollection).selectProperty(ImageCollection::nameProperty)));
		// Bind the organization property of the current collection to the organization text property
		// We cache the property so that it does not get garbage collected early
		this.txtOrganization.textProperty().bindBidirectional(cache(EasyBind.monadic(selectedCollection).selectProperty(ImageCollection::organizationProperty)));
		// Bind the contact info property of the current collection to the contact info text property
		// We cache the property so that it does not get garbage collected early
		this.txtContactInfo.textProperty().bindBidirectional(cache(EasyBind.monadic(selectedCollection).selectProperty(ImageCollection::contactInfoProperty)));
		// Bind the description property of the current collection to the description text property
		// We cache the property so that it does not get garbage collected early
		this.tbxDescription.textProperty().bindBidirectional(cache(EasyBind.monadic(selectedCollection).selectProperty(ImageCollection::descriptionProperty)));

		// Bind the permissions to the image collection's permissions
		this.tvwPermissions.itemsProperty().bind(EasyBind.monadic(selectedCollection).map(ImageCollection::getPermissions));


		// Add prompt text to the contact info and description so that users know what the fields are for
		this.txtContactInfo.setPromptText("Email and/or Phone Number preferred");
		this.tbxDescription.setPromptText("Describe the project/collection");

		// If no collection is selected, disable the text fields and buttons
		BooleanBinding nothingSelected = selectedCollection.isNull();

		// Disable this button when the selected permission is the owner
		this.btnRemoveUser.disableProperty().bind(EasyBind.monadic(this.tvwPermissions.getSelectionModel().selectedItemProperty()).selectProperty(Permission::ownerProperty).orElse(nothingSelected));

		this.clmUser.setCellValueFactory(param -> param.getValue().usernameProperty());
		this.clmUser.setCellFactory(x -> new EditCell<>(new DefaultStringConverter()));
		this.clmRead.setCellValueFactory(param -> param.getValue().readProperty());
		this.clmRead.setCellFactory(param -> new CheckBoxTableCell<>());
		this.clmUpload.setCellValueFactory(param -> param.getValue().uploadProperty());
		this.clmUpload.setCellFactory(param -> new CheckBoxTableCell<>());
		this.clmOwner.setCellValueFactory(param -> param.getValue().ownerProperty());
		this.clmOwner.setCellFactory(param -> new CheckBoxTableCell<>());
		TableColumnHeaderUtil.makeHeaderWrappable(this.clmOwner);
		this.clmOwner.setEditable(false);

		// Upon double clicking an empty cell, add a new user
		this.tvwPermissions.setRowFactory(table -> {
			TableRow<Permission> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2)
					if (row.isEmpty())
						createUser();
			});
			return row;
		});

		// Ensure that the table view is editable
		this.tvwPermissions.setEditable(true);
	}

	/**
	 * This is purely used so that the action listeners are not garbage collected early
	 *
	 * @param reference The reference to cache
	 * @param <T> The type of the property, can be anything
	 * @return Returns the property passed in purely for convenience
	 */
	private <T> Property<T> cache(Property<T> reference)
	{
		// Add the reference and return it
		this.hardReferences.add(reference);
		return reference;
	}

	public void setSelectedCollection(ImageCollection collection)
	{
		this.selectedCollection.setValue(collection);
	}

	/**
	 * When we click the add new user button
	 *
	 * @param actionEvent
	 */
	public void addNewUser(ActionEvent actionEvent)
	{
		// Just create a new user with permissions
		createUser();
		actionEvent.consume();
	}

	/**
	 * Creates a new user without a name and no permissions
	 */
	private void createUser()
	{
		// Create the permission
		Permission permission = new Permission();
		// If the selected collection is not null, add it to the collection
		if (this.selectedCollection.getValue() != null)
			this.selectedCollection.getValue().getPermissions().add(permission);
	}

	/**
	 * Revokes permission from a given user
	 *
	 * @param actionEvent
	 */
	public void removeCurrentUser(ActionEvent actionEvent)
	{
		// Grab the selected permission
		Permission selected = this.tvwPermissions.getSelectionModel().getSelectedItem();
		// If it's not null (so something is indeed selected), remove the permission
		if (selected != null)
		{
			selectedCollection.getValue().getPermissions().remove(selected);
		}
		// Otherwise show an alert that no permission was selected
		else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.tvwPermissions.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Permission Selected");
			alert.setContentText("Please select a permission from the permissions list to edit.");
			alert.showAndWait();
		}
		// Consume the event
		actionEvent.consume();
	}

	/**
	 * Temporary, pushes changes to CyVerse. Not to actually be used on release
	 * @param actionEvent
	 */
	public void saveCollection(ActionEvent actionEvent)
	{
		btnSave.setDisable(true);
		ImageCollection currentlySelected = this.selectedCollection.getValue();
		if (currentlySelected != null)
		{
			for (Permission permission : currentlySelected.getPermissions())
			{
				if (!SanimalData.getInstance().getConnectionManager().isValidUsername(permission.getUsername()))
				{
					Alert alert = new Alert(Alert.AlertType.WARNING);
					alert.setTitle("Invalid User");
					alert.setHeaderText("Username entered invalid");
					alert.setContentText("The username (" + permission.getUsername() + ") you entered was not found on the CyVerse system. Reminder: permissions are expecting usernames, not real names.");
					alert.showAndWait();
					btnSave.setDisable(false);
					return;
				}
			}

			FinishableTask<Void> saveTask = new FinishableTask<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					this.updateProgress(0, 1);
					this.updateMessage("Saving the collection: " + currentlySelected.getName());


					SanimalData.getInstance().getConnectionManager().pushLocalCollection(currentlySelected);

					this.updateProgress(0, 1);
					return null;
				}
			};
			saveTask.setOnFinished(event -> btnSave.setDisable(false));

			SanimalData.getInstance().getSanimalExecutor().addTask(saveTask);
		}
	}

	/**
	 * Transfers ownership from one user to another
	 *
	 * @param actionEvent
	 */
	public void transferOwnership(ActionEvent actionEvent)
	{
		TextInputDialog input = new TextInputDialog();
		input.setTitle("Transfer Ownership");
		input.setContentText("Enter the username of the user to transfer ownership to");

		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Invalid User");
		alert.setHeaderText("Username entered invalid");
		alert.setContentText("The username you entered was not found on the CyVerse system, please try again...");

		String newOwner;
		Boolean gotValidUsername = false;
		while (!gotValidUsername)
		{
			Optional<String> inputValue = input.showAndWait();
			if (inputValue.isPresent())
			{
				newOwner = inputValue.get();
				gotValidUsername = SanimalData.getInstance().getConnectionManager().isValidUsername(newOwner);
				if (!gotValidUsername)
					alert.showAndWait();
			}
			else
				return;
		}

		Alert transferConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
		transferConfirmation.setTitle("Transfer Confirmation");
		transferConfirmation.setHeaderText("Continue finalizing transfer?");
		transferConfirmation.setContentText("Once the owner has been set, you will no longer be able to edit collection permissions, description, title, or any other settings. Are you sure you want to continue?");
		Optional<ButtonType> buttonTypeSelected = transferConfirmation.showAndWait();
		buttonTypeSelected.ifPresent(buttonType -> {
			if (buttonType == ButtonType.OK)
			{

			}
		});
	}
}