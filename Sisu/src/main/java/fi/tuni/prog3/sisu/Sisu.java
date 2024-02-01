package fi.tuni.prog3.sisu;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;


/**
 * JavaFX Sisu
 */
public class Sisu extends Application {

    // tarvittavat yleiset muuttujat
    private JsonReader reader;
    private HashMap<String, Boolean> selectedCourses = new HashMap<>();
    private HashMap<String, GrouppingModule> groupMap= new HashMap<>();
    private HashMap<String, CourseModule> coursesMap = new HashMap<>();
    private String OpName;
    private String OpNum;
    private VBox rightVBox;
    
    @Override
    public void start(Stage stage) throws Exception {
        
        //Creating a new BorderPane.
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        Label info = new Label("Opintorakenne");
        info.setFont(new Font("Arial", 30));
        root.setTop(info);
        info.setTranslateX(350);
        
        //Adding HBox to the center of the BorderPane.
        VBox leftVBox = getLeftVBox();
        rightVBox = getRightVBox();
        root.setCenter(getCenterHbox(leftVBox, rightVBox));
        populateRightBox();
        
        //Adding button to the BorderPane and aligning it to the right.
        var quitButton = getQuitButton();
        BorderPane.setMargin(quitButton, new Insets(10, 10, 0, 10));
        root.setBottom(quitButton);
        BorderPane.setAlignment(quitButton, Pos.TOP_RIGHT);
        
        // välilehdet
        TabPane tabPane = new TabPane();
        Tab tab1 = new Tab("Opiskelija", getStudentTab());
        Tab tab2 = new Tab("Opintojen rakenne", root);
        tab1.setClosable(false);
        tab2.setClosable(false);
        tabPane.getTabs().addAll(tab1, tab2);
        
        // scenen renderöinti
        Scene scene = new Scene(tabPane, 1000, 800);                      
        stage.setScene(scene);
        stage.setTitle("SisuGUI");
        stage.show();
        
        // puulle kuuntelija
        TreeView treeView = (TreeView) scene.lookup("#tree");
        treeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> 
                        addRightFromTree(newValue, true));
    }

    public static void main(String[] args) {
        launch();
    }
    
    /**
     * muodostaa opiskelijan tiedot välilehden
     * @return BorderPane, joka pitää sisällään opiskelija välilehden
     */
    private BorderPane getStudentTab() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        
        Label name = new Label("Nimi: ");
        name.setFont(new Font("Arial", 14));
        grid.add(name, 0, 0);
        TextField TextFieldOpName = new TextField(OpName);
        TextFieldOpName.setId("TextFieldOpName");
        TextFieldOpName.textProperty().addListener((observable, oldValue, newValue) -> {
            //System.out.println("textfield changed from " + oldValue + " to " + newValue);
            OpName = newValue;
        });
        grid.add(TextFieldOpName, 1, 0);
        
        Label opNum = new Label("Opiskelijanumero: ");
        opNum.setFont(new Font("Arial", 14));
        grid.add(opNum, 0, 1);
        TextField TextFieldOpNum = new TextField(OpNum);
        TextFieldOpNum.textProperty().addListener((observable, oldValue, newValue) -> {
            //System.out.println("textfield changed from " + oldValue + " to " + newValue);
            OpNum = newValue;
        });
        grid.add(TextFieldOpNum, 1, 1);
        
        var quitButton = getQuitButton();
        
        Button search = new Button("search profile");
        search.setOnAction((ActionEvent event) -> {
            if ( reader.findUserInformation(OpName, OpNum) ){
                System.out.println("found");
                OpName = reader.getStudentNameActive();
                OpNum = reader.getStudentNumberActive();
                selectedCourses = reader.getSelectedCoursesActive();
                
                rightVBox.getChildren().remove(1, rightVBox.getChildren().size());
                //System.out.println("lapsukaiset  "+rightVBox.getChildren());
                populateRightBox();
            }
            else {
                System.out.println("not found");
                rightVBox.getChildren().remove(1, rightVBox.getChildren().size());
            }
        });
        
        FlowPane buttons = new FlowPane() ;
        buttons.getChildren().addAll( search, quitButton );
        BorderPane.setMargin(buttons, new Insets(10, 10, 0, 10));
        root.setBottom(buttons);
        BorderPane.setAlignment(buttons, Pos.TOP_RIGHT);
        
        root.setTop(grid);
        BorderPane.setAlignment(grid, Pos.CENTER);
        return root;
    }
    
    /**
     * Muodostaa kurssit välilehden keskeisen näkymän
     * @param leftVBox vasen laatikko
     * @param rightVBox oikea laatikko 
     * @return keskilaatikko, jossa vasen ja oikea laatikko
     */
    private HBox getCenterHbox(VBox leftVBox, VBox rightVBox) {
        //Creating an HBox.
        HBox centerHBox = new HBox(10);
        
        //Adding two VBox to the HBox.
        centerHBox.getChildren().addAll(leftVBox, rightVBox);
        
        return centerHBox;
    }
    
    /**
     * Muodostaa vasemman laatikon
     * @return VBox vasen laatikko, joka pitää sisällään kurssien puunäkymän
     * @throws Exception jos tiedostojen lukemisessa tapahtuu ongelma
     */
    private VBox getLeftVBox() throws Exception {
        //Creating a VBox for the left side.
        VBox leftVBox = new VBox(5);
        leftVBox.setPrefWidth(480);
        leftVBox.setStyle("-fx-background-color: #8fc6fd;");
        
        Label label = new Label("Kurssikokoelma");
        label.setFont(new Font("Arial", 18));
        
        TreeView<String> tv = populateLeftBox();
        VBox.setVgrow(tv, Priority.ALWAYS);
        leftVBox.getChildren().addAll(label,tv);
        
        leftVBox.setAlignment(Pos.CENTER);
        
        return leftVBox;
    }
    
    /**
     * muodostaa oikean laatikon 
     * @return oikean laatikon perusmalli
     */
    private VBox getRightVBox() {
        //Creating a VBox for the right side.
        VBox rightVBox = new VBox(5);
        rightVBox.setId("rightVBox1");
        rightVBox.setPrefWidth(480);
        rightVBox.setStyle("-fx-background-color: #b1c2d4;");
        
        Label label = new Label("Valitut kurssit");
        label.setFont(new Font("Arial", 18));
        label.setTranslateX(170);
        rightVBox.getChildren().add(label);

        return rightVBox;
    }
    
    /**
     * Muodostaa poistumisnäppäimen
     * @return näppäin, jolla ohjelman voi sulkea 
     */
    private Button getQuitButton() {
        //Creating a button.
        Button button = new Button("Quit");
        
        //Adding an event to the button to terminate the application.
        // samalal myös tallenetaan käyttäjän  tiedot
        button.setOnAction((ActionEvent event) -> {
            try {
                //System.out.println("Name: "+OpName+" Opnum: "+OpNum);
                reader.setActiveUserProfile(OpName, OpNum, selectedCourses);
                reader.writeToFile(OpName+OpNum+".json");
            } catch (Exception ex) {
                
            }
            Platform.exit();
        });
        
        return button;
    }
    
    /**
     * Täydentää oikeaa laatikkoa valittujen kurssien tiedoilla
     */
    private void populateRightBox(){
        
        for ( String course : selectedCourses.keySet() ){
            //System.out.println(course);
            addRightFromTree(course, false);
        }
    }
    
    /**
     * Hakee tiedot tietyn ryhmän alle kuuluvista kursseista
     * @param courseMod kurssimoduuli, jolle haetaan tiedot
     * @return puuitemin, joka sisältää kurssit tiedot oikeassa muodossa
     */
    private TreeItem<String> getCourseTree(CourseModule courseMod){
        
        String courseString = String.format("%s %dop ", 
            courseMod.getName(), courseMod.getMinCredits());
        TreeItem<String> courseTree = new TreeItem<>(courseString );
        return courseTree;
    }
    
    /**
     * Hakee tiedot tietylle ryhmälle ja muodostaa niistä puuitemin
     * @param groupMod ryhmä moduuli, jonka tietoja haetaan
     * @return puuitemi, joka sisältää ryhmän alaryhmät ja kurssit 
     */
    private TreeItem<String> getGroupTree(GrouppingModule groupMod ){
        // alkupuu
        TreeItem<String> groupTree = new TreeItem<>(groupMod.getName() );
        
        // onko lapsia 
        ArrayList<String> childs = groupMod.getChildIds();
        if ( !childs.isEmpty() ){
            for ( String child : childs ){
                
                // jos kursseja -> lisätään 
                if ( coursesMap.containsKey(child) ){ 
                    
                    CourseModule courseMod = coursesMap.get(child);
                    groupTree.getChildren().add(getCourseTree(courseMod));
                    }
                // jos ryhmiä -> rekursioo
                if ( groupMap.containsKey(child) ){
                    GrouppingModule groupMod2 = groupMap.get(child);
                    TreeItem<String> level3 = getGroupTree(groupMod2);
                    groupTree.getChildren().add(level3);
                }
            }
        }
        return groupTree;
    }
    
    /**
     * Täydentää vasemman laatikon kurssien tiedoilla puurakenteena
     * @return palauttaa puurakenteen kursseista
     * @throws Exception jos tietojen lukemisessa tapahtuu virhe
     */
    private TreeView<String> populateLeftBox() throws Exception{
         // alkupuut
        TreeItem<String> root = new TreeItem<>("SISU");
        
        // luetaan JSONit ja tehään puu
        reader = new JsonReader();
        if ( reader.readFromSisu() ){
            
            groupMap = reader.getGrouppings();
            coursesMap = reader.getCourses();
            
            if ( reader.findUserInformation(OpName, OpNum) ){
                OpName = reader.getStudentNameActive();
                OpNum = reader.getStudentNumberActive();
            } 
            else {
                reader.setActiveUserProfile("", "", new HashMap<String, Boolean>());
            }
            
            // käydään läpi pääryhmät
            ArrayList<String> degreeIDs = reader.getDegreeProgrammeIds();
            for ( var group : degreeIDs ){
                
                if ( groupMap.containsKey(group) ){
                    GrouppingModule gg = groupMap.get(group);
                    TreeItem<String> level2 = getGroupTree(gg);
                    root.getChildren().add(level2);
                }
            }
        }
        
        // loput puun hierarkiasta ja asetuksista
        root.setExpanded(true);
        TreeView<String> treeView = new TreeView<>(root);
        treeView.setId("tree");
        treeView.setShowRoot(false);
        treeView.resize(400, 1000);
        
        return treeView;
    }
    
    /**
     * Lisää oikeaan laatikkoon vasemmasta puusta, klikkausksen yhteydessä
     * @param newValue uusi arvo, joka lisätään
     * @param add totuusarvo sille, että lisätäänkö kurssi valittuihin kursseihin vai ei
     */
    private void addRightFromTree(Object newValue, Boolean add){
        String course = newValue.toString();
        
        if ( add ){
            course = course.substring(18, course.length()-1);
            if ( !selectedCourses.containsKey(course) ){
                selectedCourses.put(course, Boolean.FALSE);
            }
            else {
                return;
            }
        }
        //System.out.println(course);
        if ( !course.substring(course.length()-2).equals("  ") ){
            return;
        }
        
        // label säätöjä
        Label label = new Label(course);
        label.setFont(new Font("Arial", 15));
        
        // checkbox siihen kivasti viereen
        CheckBox checkbox = new CheckBox();
        if ( !add ){
            if ( selectedCourses.get(course) ){
                checkbox.setSelected(true);
            }
        }
        // lisätään molemmat Hboxiin
        HBox checkAndName = new HBox(checkbox, label);
        
        // eventhandleri poistamista varten
        label.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event e){
                //System.out.println(e.toString());
                //checkAndName.getChildren().remove(label);
                rightVBox.getChildren().remove(label.getParent());
                System.out.println(label.toString());
                
                if ( selectedCourses.containsKey(label.getText()) ){
                    selectedCourses.remove(label.getText());
                }
            }
        });
        
        // kurssinimen nimeäminen toiseen muuttujaan lambdan takia 
        String c = course;
        // checkbox tapahtumat
        checkbox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                
                if ( checkbox.isSelected() ){
                    //System.out.println("klikattu päälle  "+checkAndName.getChildren().get(1).toString());
                    selectedCourses.replace(c, true);
                }
                else {
                    selectedCourses.replace(c, false);
                }
            }
        });
        
        // lisäys oikeelle
        rightVBox.getChildren().add(checkAndName);
    }
    
}