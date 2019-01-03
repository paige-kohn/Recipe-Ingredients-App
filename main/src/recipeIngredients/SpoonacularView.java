package recipeIngredients;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;


import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

@Singleton

public class SpoonacularView extends JFrame {
    private JTextArea recipeInfo;
    private JLabel recipeTitle;
    private JLabel foodJoke;
    private JTextField[] ingredientsEntered = new JTextField[5];
    private JTextArea recipeSummary1;
    private JTextArea recipeSummary2;
    private int recipeID;
    private Map<String, Integer>  recipeIDMap = new HashMap<>();
    private DefaultListModel<String> model1 = new DefaultListModel<String>();
    private DefaultListModel<String> model2 = new DefaultListModel<String>();
    private JList<String> recipeList1;
    private JList<String> recipeList2;
    private String mode;
    private String keyword;
    private JTextField keywordField;

    @Inject
    public SpoonacularView(SpoonacularController controller) {
        setLocation(240, 180);
        setSize(1000, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Recipe Box ...");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                controller.stop();
            }
        });
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JTabbedPane source = (JTabbedPane) event.getSource();
                int index = source.getSelectedIndex();
                mode = source.getTitleAt(index);
            }
        };

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(changeListener);
        tabbedPane.setBackground(Color.black);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 30, 10);

        JPanel ingredientsPanel = new JPanel(fl);
        JPanel keywordPanel = new JPanel(fl);
        JPanel recipePanel1 = new JPanel(new GridLayout(0, 1));
        JPanel recipePanel2 = new JPanel(new GridLayout(0, 1));
        JPanel tab1 = new JPanel(new GridLayout(0, 2));
        JPanel tab2 = new JPanel(new GridLayout(0, 2));

        foodJoke = new JLabel();
        //controller.getRandomJoke();

        keywordField = new JTextField();
        keywordField.setColumns(15);

        recipeSummary1 = new JTextArea();
        recipeSummary1.setWrapStyleWord(true);
        recipeSummary1.setLineWrap(true);
        recipeSummary1.setColumns(30);
        recipeSummary1.setRows(55);
        recipeList1 = new JList<String>(model1);
        recipeList1.setSelectionMode(SINGLE_SELECTION);
        recipeList1.setLayoutOrientation(JList.VERTICAL);
        recipeList1.addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                JList changedList = (JList) e.getSource();
                Object item = changedList.getSelectedValue();
                recipeID = recipeIDMap.get(item.toString());
                controller.getQuickSummary(recipeID);
                keywordPanel.add(recipeSummary1);
            }


        });
        keywordPanel.add(new JLabel("Search for..."));
        keywordPanel.add(keywordField);
        keywordPanel.add(recipeSummary1);
        recipePanel1.add(recipeList1);
        tab1.add(keywordPanel);
        tab1.add(recipePanel1);

        ingredientsPanel.add(new JLabel("  Enter up to 5 ingredients: "));
        for (int i = 0; i < ingredientsEntered.length; i++) {
            ingredientsEntered[i] = new JTextField();
            ingredientsEntered[i].setColumns(25);
            ingredientsPanel.add(ingredientsEntered[i]);
        }
        recipeSummary2 = new JTextArea();
        recipeSummary2.setWrapStyleWord(true);
        recipeSummary2.setLineWrap(true);
        recipeSummary2.setColumns(30);
        recipeSummary2.setRows(34);
        recipeList2 = new JList<String>(model2);
        recipeList2.setSelectionMode(SINGLE_SELECTION);
        recipeList2.setLayoutOrientation(JList.VERTICAL);
        recipeList2.addListSelectionListener((ListSelectionEvent e) -> {

            if (!e.getValueIsAdjusting()) {
                JList changedList = (JList) e.getSource();
                Object item = changedList.getSelectedValue();
                recipeID = recipeIDMap.get(item.toString());
                controller.getQuickSummary(recipeID);
                ingredientsPanel.add(recipeSummary2);
            }
        });
        recipePanel2.add(recipeList2);
        ingredientsPanel.add(recipeSummary2);
        tab2.add(ingredientsPanel);
        tab2.add(recipePanel2);

        tabbedPane.add("Search for a recipe", tab1);
        tabbedPane.add("Lookup recipes by ingredient", tab2);


        JPanel mainPanel = new JPanel(new BorderLayout());
        JButton searchButton = new JButton("Search");
        mainPanel.add(searchButton, BorderLayout.AFTER_LAST_LINE);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mode.equals("Search for a recipe")) {
                    keyword = keywordField.getText();
                    findRecipesByKeyword(controller, keyword);
                    recipePanel1.add(recipeList1);
                    displayRecipeInfoDialog(controller, recipeList1);

                }

                if (mode.equals("Lookup recipes by ingredient")) {
                    StringBuilder ingredientsBuilder = new StringBuilder();
                    for (int i = 0; i < ingredientsEntered.length; i++) {
                        ingredientsBuilder.append(ingredientsEntered[i].getText()).append(",");
                        findByIngredients(controller, ingredientsBuilder.toString());
                        recipePanel2.add(recipeList2);
                        displayRecipeInfoDialog(controller, recipeList2);

                    }

                }

            }
        });



        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        Border border = BorderFactory.createEmptyBorder(20, 10, 20, 10);
        getRootPane().setBorder(border);
        add(mainPanel);

    }

    public void findRecipesByKeyword(SpoonacularController controller, String keyword) {
        controller.getRecipesByKeyword(keyword);
    }

    public void findByIngredients(SpoonacularController controller, String ingredients) {
        controller.findByIngredients(ingredients);
    }


    public void displayRecipeInfoDialog(SpoonacularController controller, JList recipeList){

        recipeList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList list = (JList)e.getSource();
                if (e.getClickCount() == 2) {
                    controller.getRecipeInformation(recipeID);
                    createDialog();
                }
            }

        });


    }

    private void createDialog(){
        JDialog d = new JDialog();
        recipeTitle = new JLabel();
        recipeInfo = new JTextArea();
        recipeInfo.setLineWrap(true);
        recipeInfo.setWrapStyleWord(true);
        d.setSize(700 ,500);
        d.setTitle(recipeTitle.getText());
        d.add(recipeInfo);
        d.setLocationRelativeTo(SpoonacularView.this);
        d.setVisible(true);
    }


    public void showRecipes(RecipeInformation recipeInformation) {

        StringBuilder recipeBuilder = new StringBuilder();
        recipeBuilder.append("\t").append(recipeInformation.getTitle()).append("\n\n");
        for (int i = 0; i < recipeInformation.getExtendedIngredients().size(); i++) {
            recipeBuilder.append("  ");
            recipeBuilder.append(recipeInformation.getExtendedIngredients().get(i).getOriginalString());
            recipeBuilder.append("\n");
        }
        recipeInfo.setText(recipeBuilder.toString());
        recipeTitle.setText(recipeInformation.getTitle());
        recipeID = recipeInformation.getId();
    }


    public void showFindByIngredient(ArrayList<Recipe> feed) {
        for (int i = 0; i < feed.size(); i++) {
            String recipe = " "+feed.get(i).getTitle() + "\n";
            model2.add(i, recipe);
            recipeIDMap.putIfAbsent(recipe, feed.get(i).getId());
        }
    }

    public void showRecipesByKeyword(SpoonacularFeed feed) {
        for (int i = 0; i < feed.getRecipeList().size(); i++) {
            String recipe = " "+ feed.getRecipeList().get(i).getTitle() + "\n";
            model1.add(i, recipe);
            recipeIDMap.putIfAbsent(recipe, feed.getRecipeList().get(i).getId());
        }

    }

    public void showQuickSummary(Recipe recipe) {
        String summary = recipe.getSummary().replaceAll("<[^>]*>", "");
        String title = "\n"+ recipe.getTitle();
        if(mode.equals("Search for a recipe")){
            recipeSummary1.setText(title + "\n"+ summary);
        }
        if(mode.equals("Lookup recipes by ingredient")){
            recipeSummary2.setText(title + "\n"+ summary);
        }
    }

    public void setJoke(SpoonacularFeed feed) {
        String joke = feed.getJoke();
        foodJoke.setText(joke + "...");
    }




    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new SpoonacularModule());
        SpoonacularView view = injector.getInstance(SpoonacularView.class);
        view.setVisible(true);

    }
}
