package controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ToggleGroup;
import model.Goal;
import model.GoalDBModel;

public class TargetGoalController implements Initializable {

    @FXML
    private JFXTextField txtGoalName;

    @FXML
    private JFXTextField txtObjAmount;

    @FXML
    private DatePicker dpStartDate;

    @FXML
    private DatePicker dpEndDate;

    @FXML
    private JFXTextField txtSaveAmount;

    @FXML
    private JFXRadioButton rbDaily;

    @FXML
    private ToggleGroup tgSaveType;

    @FXML
    private JFXRadioButton rbWeekly;

    @FXML
    private JFXRadioButton rbMonthly;
    
    @FXML
    private JFXButton btnCreateGoal;
    
    private LocalDate startDate = LocalDate.now();
    
    private LocalDate endDate = LocalDate.now().plus(1,ChronoUnit.DAYS);
    
    private String saveType = "Daily";
    
    private int objAmount = 0;
    
    private double period = 1;
    
    private boolean changeDate = true;

    @FXML
    void convertCurrency(ActionEvent event) {

    }

    @FXML
    void createGoal(ActionEvent event) {
    	String goalName = txtGoalName.getText();
    	double amountToSave = Double.valueOf(txtSaveAmount.getText());
    	Goal newGoal = new Goal(goalName, this.objAmount, this.startDate.toString(), this.endDate.toString(), this.saveType, amountToSave, 0, 1);
    	GoalDBModel goalModel = new GoalDBModel();
    	goalModel.insertGoal(newGoal);
    }

    @FXML
    void processAbout(ActionEvent event) {

    }
    
    @FXML
    void processStartDate(ActionEvent event) {
    	this.startDate = dpStartDate.getValue();
    }
    
    private void disableSaveTypes(boolean weekly, boolean monthly) {
    	rbMonthly.setDisable(monthly);
    	rbWeekly.setDisable(weekly);
    }

    @FXML
    void processEndDate(ActionEvent event) {
    	if(changeDate) {
    		this.endDate = dpEndDate.getValue();
        	
        	//Total number of target days 
        	this.period = ChronoUnit.DAYS.between(this.startDate, this.endDate);
        	
        	if(this.period < 7) {
        		this.disableSaveTypes(true, true);//first argument is for weekly save type radio button, second argument is for monthly save type radio button
        	}else if(this.period < 30) {
        		this.disableSaveTypes(false, true);
        	}else {
        		this.disableSaveTypes(false, false);
        	}
        	processSaveType(event);
    	}else {
    		changeDate = true;
    	}
    	
    }
    
    @FXML
    void processSaveType(ActionEvent event) {
    	this.changeDate = false;
    	if(rbWeekly.isSelected()) {
    		this.saveType = "Weekly";
    	}
    	else if(rbMonthly.isSelected()) {
    		this.saveType = "Monthly";
    	}
    	else {
    		this.saveType = "Daily";
    	}
    	this.calculateSaveAmount();
    }
    
    private void calculateSaveAmount() {
    	int noOfPeriod = 1;
    	switch(this.saveType) {
	    	case "Daily":
    			txtSaveAmount.setText(String.valueOf(this.objAmount/this.period));
	    		break;
	    	case "Weekly":
	    		noOfPeriod = (int)this.period/7;
	    		txtSaveAmount.setText(String.valueOf(this.objAmount/noOfPeriod));
	    		break;
	    	case "Monthly":
	    		noOfPeriod = (int)this.period/30;
	    		txtSaveAmount.setText(String.valueOf(this.objAmount/noOfPeriod));
	    		break;
    	}
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		dpStartDate.setValue(LocalDate.now()); //Set current date in date picker for start date
		dpEndDate.setValue(LocalDate.now().plus(1,ChronoUnit.DAYS));
		
		txtSaveAmount.textProperty().addListener(new ChangeListener<Object>() {

			@Override
			public void changed(ObservableValue<? extends Object> observableValue, Object oldValue, Object newValue) {
				if(changeDate) {
					double saveAmount = Double.valueOf(txtSaveAmount.getText());
					LocalDate today = LocalDate.now();
					LocalDate amountToAdd = null;
					int noOfDays = (int) Math.ceil(objAmount/saveAmount);
					
					if(rbDaily.isSelected()) {
						amountToAdd = today.plus(noOfDays,ChronoUnit.DAYS);
					}else if(rbWeekly.isSelected()) {
						amountToAdd = today.plus(noOfDays*7,ChronoUnit.DAYS);
					}else if(rbMonthly.isSelected()) {
						amountToAdd = today.plus(noOfDays*30,ChronoUnit.DAYS);
					}
					changeDate = false;
					dpEndDate.setValue(amountToAdd);
				}else {
					changeDate = true;
				}
				
			}
		});
		
		//get Object Amount when user is entering in text field
		txtObjAmount.textProperty().addListener(new ChangeListener<Object>() {

			@Override
			public void changed(ObservableValue<? extends Object> observableValue, Object oldValue, Object newValue) {
				objAmount = Integer.valueOf(txtObjAmount.getText());
				calculateSaveAmount();
			}
		});
		
		btnCreateGoal.disableProperty().bind((
				txtGoalName.textProperty().isNotEmpty().and(
				txtObjAmount.textProperty().isNotEmpty()).and(
				txtSaveAmount.textProperty().isNotEmpty())).not());
	}

}
