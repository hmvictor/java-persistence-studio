package jpql.studio;

import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import jpql.studio.renderer.BooleanRenderer;
import jpql.studio.renderer.DateRenderer;
import jpql.studio.renderer.ManagedTypeRenderer;
import jpql.studio.renderer.NumberRenderer;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//jpql.studio//QueryEditor//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "QueryEditorTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "jpql.studio.QueryEditorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_QueryEditorAction"
)
@Messages({
    "CTL_QueryEditorAction=QueryEditor",
    "CTL_QueryEditorTopComponent=QueryEditor Window",
    "HINT_QueryEditorTopComponent=This is a QueryEditor window"
})
public final class QueryEditorTopComponent extends TopComponent {

    private EntityManagerFactory factory;
    private EntityManager entityManager;

    private Project project;
    private ClassLoader projectClassloader;
    private DefaultTableModel emptyTableModel = new DefaultTableModel(new String[]{"0"}, 0);
    private static final Logger logger = Logger.getLogger(QueryEditorTopComponent.class.getName());

    private final AbstractAction executeAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            resultTable.setModel(emptyTableModel);
            output.setText(null);
            ProgressHandle handle = ProgressHandleFactory.createHandle("Executing");
            handle.start();
            handle.switchToIndeterminate();
            Executor swingExecutor = runnable -> {
                if (SwingUtilities.isEventDispatchThread()) {
                    runnable.run();
                } else {
                    SwingUtilities.invokeLater(runnable);
                }
            };
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            
            CompletableFuture.supplyAsync(() -> {
                if (factory == null) {
                    factory = new EntityManagerFactorySuplier(projectClassloader, getSelectedPersistenceUnit(), getSelectedConnection()).get();
                }
                return factory;
            }, executorService).thenCompose(ef -> CompletableFuture.supplyAsync(() -> {
                if (entityManager == null) {
                    entityManager = ef.createEntityManager();
                }
                return entityManager;
            }, executorService)).thenCompose(em -> 
                CompletableFuture.supplyAsync(new QueryCreator(em, getCurrentQuery())::create, executorService)
            ).thenCompose(query -> 
                CompletableFuture.supplyAsync(new QueryExecutor(query, getFirstResult(), getMaxResults())::getResult, executorService)
            ).thenAcceptAsync(
                QueryEditorTopComponent.this::setQueryResult, swingExecutor
            ).exceptionally(t -> { //query execution
                output.setText("Error when executing query: " + t.toString());
                resultTabbedPane.setSelectedIndex(1);
                logger.log(Level.SEVERE, t.toString(), t);
                return null;
            }).handle((result, ex) -> {
                executeAction.setEnabled(true);
                handle.finish();
                executorService.shutdown();
                return null;
            });

//            CompletableFuture
//                    .supplyAsync(() -> {
//                        if (factory == null) {
//                            factory = new EntityManagerFactorySuplier(projectClassloader, getSelectedPersistenceUnit(), getSelectedConnection()).get();
//                        }
//                        return factory;
//                    }, executorService).thenAcceptAsync(ef -> {
//                CompletableFuture.supplyAsync(() -> {
//                    if (entityManager == null) {
//                        entityManager = ef.createEntityManager();
//                    }
//                    return entityManager;
//                }, executorService).thenAcceptAsync(em -> {
//                    CompletableFuture
//                            .supplyAsync(new QueryCreator(em, getCurrentQuery())::create, executorService)
//                            .thenAcceptAsync((query) -> {
//                                CompletableFuture
//                                        .supplyAsync(new QueryExecutor(query, getFirstResult(), getMaxResults())::getResult, executorService)
//                                        .thenAcceptAsync(QueryEditorTopComponent.this::setQueryResult, swingExecutor)
//                                        .exceptionally((Throwable t) -> { //query execution
//                                            output.setText("Error when executing query: " + t.toString());
//                                            resultTabbedPane.setSelectedIndex(1);
//                                            logger.log(Level.SEVERE, t.toString(), t);
//                                            return null;
//                                        });
//                            }, executorService).exceptionally((Throwable t) -> { //query creation
//                        output.setText("Error when creating query: " + t.toString());
//                        resultTabbedPane.setSelectedIndex(1);
//                        logger.log(Level.SEVERE, t.toString(), t);
//                        return null;
//                    });
//                }, executorService).exceptionally((Throwable t) -> { //entityManager
//                    output.setText("Error when creating entity manager: " + t.toString());
//                    resultTabbedPane.setSelectedIndex(1);
//                    logger.log(Level.SEVERE, t.toString(), t);
//                    return null;
//                });
//            }).exceptionally((Throwable t) -> { //factory
//                output.setText("Error when creating entity manager factory: " + t.toString());
//                resultTabbedPane.setSelectedIndex(1);
//                logger.log(Level.SEVERE, t.toString(), t);
//                return null;
//            });

        }

    };

    public QueryEditorTopComponent() {
        initComponents();
        setName(Bundle.CTL_QueryEditorTopComponent());
        setToolTipText(Bundle.HINT_QueryEditorTopComponent());
    }

    public QueryEditorTopComponent(Project project) {
        this.project = project;
        projectClassloader = new PersistenceClassloaderBuilder().getClassloader(project, EntityManagerFactory.class.getClassLoader());
        initComponents();
        setName(Bundle.CTL_QueryEditorTopComponent());
        setToolTipText(Bundle.HINT_QueryEditorTopComponent());
        initOptions(project);
        setDisplayName(ProjectUtils.getInformation(project).getDisplayName());
        textMaxResults.setInputVerifier(new PositiveIntegerVerifier());
        textFirstResult.setInputVerifier(new PositiveIntegerVerifier());
        resultTable.setModel(emptyTableModel);
    }

    public void setQueryResult(QueryResult queryResult) {
        resultTable.setModel(new QueryResultModel(queryResult));
        for (int i = 0; i < queryResult.getTypes().length; i++) {
            TableCellRenderer renderer;
            Class type = queryResult.getTypes()[i];
            boolean managed;
            try {
                factory.getMetamodel().managedType(type);
                managed = true;
            } catch (IllegalArgumentException ex) {
                managed = false;
            }
            if (managed) {
                renderer = new ManagedTypeRenderer();
            } else if (type.equals(Boolean.class)) {
                renderer = new BooleanRenderer();
            } else if (type.equals(Date.class)) {
                renderer = new DateRenderer();
            } else if (type.equals(Byte.class) || type.equals(Short.class) || type.equals(Integer.class) || type.equals(Long.class)) {
                renderer = new NumberRenderer(NumberFormat.getIntegerInstance());
            } else if (type.equals(Float.class) || type.equals(Double.class)) {
                renderer = new NumberRenderer(NumberFormat.getNumberInstance());
            } else {
                renderer = new DefaultTableCellRenderer();
            }
            resultTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        resultTabbedPane.setSelectedIndex(0);
    }

    public int getFirstResult() {
        try {
            return Integer.parseInt(textFirstResult.getText());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public int getMaxResults() {
        try {
            return Integer.parseInt(textMaxResults.getText());
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    public String getSelectedPersistenceUnit() {
        return (String) persistenceUnitOptions.getSelectedItem();
    }

    public DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection) connectionOptions.getSelectedItem();
    }

    private void initOptions(Project project) {
        connectionOptions.setModel(new ConnectionsLoader().createConnectionsModel());
        persistenceUnitOptions.setModel(new PersistenceReader().getPersistenceUnitnamesModel(project, projectClassloader));
    }

    public Project getProject() {
        return project;
    }

    public String getCurrentQuery() {
        String query = queryEditor.getSelectedText();
        if (query == null) {
            query = Queries.getQuery(queryEditor.getText(), queryEditor.getCaretPosition());
        }
        return query;
    }

    @Override
    protected void componentClosed() {
        //TODO call close methods
        if (entityManager != null) {
            entityManager.close();
        }
        if (factory != null) {
            factory.close();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionOptions = new javax.swing.JComboBox();
        persistenceUnitOptions = new javax.swing.JComboBox();
        executeButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        queryEditor = new javax.swing.JEditorPane();
        resultTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        output = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        textFirstResult = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        textMaxResults = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        connectionOptions.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        persistenceUnitOptions.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        executeButton.setAction(executeAction);
        org.openide.awt.Mnemonics.setLocalizedText(executeButton, org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.executeButton.text")); // NOI18N

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        queryEditor.setText(org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.queryEditor.text")); // NOI18N
        jScrollPane2.setViewportView(queryEditor);

        jSplitPane1.setLeftComponent(jScrollPane2);

        resultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(resultTable);

        resultTabbedPane.addTab(org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.jScrollPane3.TabConstraints.tabTitle"), jScrollPane3); // NOI18N

        output.setEditable(false);
        jScrollPane1.setViewportView(output);

        resultTabbedPane.addTab(org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.jScrollPane1.TabConstraints.tabTitle"), jScrollPane1); // NOI18N

        jSplitPane1.setRightComponent(resultTabbedPane);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.jLabel2.text")); // NOI18N

        textFirstResult.setColumns(3);
        textFirstResult.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));
        textFirstResult.setText(org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.textFirstResult.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.jLabel3.text")); // NOI18N

        textMaxResults.setColumns(3);
        textMaxResults.setText(org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.textMaxResults.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.jLabel4.text")); // NOI18N

        jTextField1.setEditable(false);
        jTextField1.setColumns(6);
        jTextField1.setText(org.openide.util.NbBundle.getMessage(QueryEditorTopComponent.class, "QueryEditorTopComponent.jTextField1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFirstResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textMaxResults, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(executeButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(connectionOptions, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(persistenceUnitOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectionOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(persistenceUnitOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(executeButton)
                    .addComponent(jLabel2)
                    .addComponent(textFirstResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(textMaxResults, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connectionOptions;
    private javax.swing.JButton executeButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane output;
    private javax.swing.JComboBox persistenceUnitOptions;
    private javax.swing.JEditorPane queryEditor;
    private javax.swing.JTabbedPane resultTabbedPane;
    private javax.swing.JTable resultTable;
    private javax.swing.JFormattedTextField textFirstResult;
    private javax.swing.JFormattedTextField textMaxResults;
    // End of variables declaration//GEN-END:variables

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
