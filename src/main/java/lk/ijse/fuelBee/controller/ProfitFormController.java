package lk.ijse.fuelBee.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.fuelBee.bo.BOFactory;
import lk.ijse.fuelBee.bo.custom.IncomeBO;
import lk.ijse.fuelBee.bo.custom.OutcomeBO;
import lk.ijse.fuelBee.bo.custom.ReportsBO;
import lk.ijse.fuelBee.dao.custom.IncomeDAO;
import lk.ijse.fuelBee.dao.custom.OutcomeDAO;
import lk.ijse.fuelBee.dao.custom.impl.IncomeDAOImpl;
import lk.ijse.fuelBee.dao.custom.impl.OutcomeDAOImpl;
import lk.ijse.fuelBee.dto.IncomeDto;
import lk.ijse.fuelBee.dto.OutcomeDto;
import lk.ijse.fuelBee.dto.tm.IncomeTm;
import lk.ijse.fuelBee.dto.tm.OutcomeTm;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ProfitFormController {
    public TableView<IncomeTm> tblIncome;
    public TableColumn<?,?> colIncomeId;
    public TableColumn<?,?> colIncomeAmount;
    public TableColumn<?,?> colIncomeDate;
    @FXML
    public TableView<OutcomeTm> tblOutcome;
    public TableColumn<?,?> colOutcomeId;
    public TableColumn<?,?> colOutcomeAmount;
    public TableColumn<?,?> colOutcomeDate;
    public DatePicker dpStartDate;
    public DatePicker dpEndDate;
    IncomeBO incomeBO = (IncomeBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.INCOME);
    OutcomeBO outcomeBO = (OutcomeBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.OUTCOME);
    ReportsBO reportsBO = (ReportsBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.REPORT);
    public void initialize() throws SQLException {
        setAllOutcome();
        setAllIncome();

        setCellValueFactory();
    }
    public void setCellValueFactory() {
        colIncomeId.setCellValueFactory(new PropertyValueFactory<>("incomeId"));
        colIncomeAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colIncomeDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colOutcomeId.setCellValueFactory(new PropertyValueFactory<>("outcomeId"));
        colOutcomeAmount.setCellValueFactory(new PropertyValueFactory<>("outcomeAmount"));
        colOutcomeDate.setCellValueFactory(new PropertyValueFactory<>("outcomeDate"));
    }
  /*  public static String generateOutcomeId() {
        String prefix = "OUT";
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
        String timestampString = dateFormat.format(new Date(timestamp));
        Random random = new Random();
        int randomComponent = random.nextInt(1000);
        String outcomeId = prefix + timestampString + randomComponent;
        return outcomeId;
    }*/
    /*public static String generateIncomeId() {
        String prefix = "INC";
        long timestamp1 = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
        String timestampString = dateFormat.format(new Date(timestamp1));
        Random random = new Random();
        int randomComponent = random.nextInt(1000);
        String incomeId = prefix + timestampString + randomComponent;
        return incomeId;
    }*/

    public void setAllOutcome() throws SQLException {
        ObservableList<OutcomeTm> obList1 = FXCollections.observableArrayList();
        ArrayList<OutcomeDto> allOutcomes = null;
        try {
            allOutcomes = outcomeBO.getAllOutcomes();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (OutcomeDto outcomeDto : allOutcomes) {
            if (outcomeDto != null) {
                obList1.add(new OutcomeTm(
                    outcomeDto.getOutcomeId(),
                    outcomeDto.getOutcomeAmount(),
                    outcomeDto.getOutcomeDate()
                ));
            }
        }
        if (tblOutcome != null) {
            tblOutcome.setItems(obList1);
            tblOutcome.refresh();
        } else {
            System.out.println("tblOutcome is null");
        }
        tblOutcome.setItems(obList1);
        tblOutcome.refresh();
    }

    public void setAllIncome() throws SQLException {
        ObservableList<IncomeTm> obList = FXCollections.observableArrayList();
        ArrayList<IncomeDto> allIncomes = null;
        try {
            allIncomes = incomeBO.getAllIncome();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (IncomeDto incomeDto : allIncomes) {
            obList.add(new IncomeTm(
                    incomeDto.getIncomeId(),
                    incomeDto.getAmount(),
                    incomeDto.getDate()
            ));
        }
        tblIncome.setItems(obList);
        tblIncome.refresh();
    }
    public void btnProfitReportOnAction(ActionEvent actionEvent) throws JRException, SQLException {

        Double totalIncome = 0.0;
        Double totalOutcome = 0.0;
        ArrayList<String> IncomeIds = new ArrayList<>();
        ArrayList<String> IncomeAmounts = new ArrayList<>();
        ArrayList<String> outcomeIds = new ArrayList<>();
        ArrayList<Object> outcomeAmounts = new ArrayList<>();

        ArrayList<IncomeDto> allIncomes;
        ArrayList<OutcomeDto> allOutcomes;
        if(dpStartDate.getValue()!=null && dpEndDate.getValue()!=null){
            java.util.Date startDate = Date.valueOf(dpStartDate.getValue());
            java.util.Date endDate = Date.valueOf(dpEndDate.getValue());
            try {
                allIncomes = incomeBO.getAllIncomesByDate(startDate, endDate);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                allOutcomes = outcomeBO.getAllOutcomesByDate(startDate, endDate);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }else{
            try {
                allIncomes = incomeBO.getAllIncome();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                allOutcomes = outcomeBO.getAllOutcomes();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        for (IncomeDto dto : allIncomes) {
            IncomeIds.add(dto.getIncomeId());
            IncomeAmounts.add(String.format("%.0f", dto.getAmount()));
            totalIncome += dto.getAmount();
        }
        for(OutcomeDto dto:allOutcomes){
            outcomeIds.add(dto.getOutcomeId());
            outcomeAmounts.add(String.format("%.0f", dto.getOutcomeAmount()));//String.valueOf(dto.getOutcomeAmount()) > String.format("%.0f", dto.getOutcomeAmount())
            totalOutcome += dto.getOutcomeAmount();
        }
        Double finalTotal=totalIncome-totalOutcome;


        HashMap<String, Object> map = new HashMap<>();
        map.put("incomeList", IncomeIds);
        map.put("addressList", IncomeAmounts);
        map.put("outcomeList", outcomeIds);
        map.put("outcomeAmountList", outcomeAmounts);
        map.put("tolIncome", String.format("%.0f",totalIncome));// String.format("%.0f",totalIncome)
        map.put("tolOutcome", String.format("%.0f",totalOutcome));
        map.put("finalTotal", String.format("%.0f",finalTotal));

        try {
            reportsBO.ProfitReport(map);
            /*InputStream resourceAsStream = getClass().getResourceAsStream("/report/financialReport1.jrxml");
            JasperDesign load = JRXmlLoader.load(resourceAsStream);
            JasperReport compileReport = JasperCompileManager.compileReport(load);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    compileReport,
                    map,
                    new JREmptyDataSource());
            JasperViewer.viewReport(jasperPrint, false);*/
        } catch (JRException e) {
            e.printStackTrace();
        }

    }
}
