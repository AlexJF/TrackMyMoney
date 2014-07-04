/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.importexport;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.exceptions.ExportException;
import net.alexjf.tmm.exceptions.ImportException;

import android.util.Log;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CSVImportExport extends ImportExport {
    private static final String ELEM_MONEYNODE_LABEL = "MoneyNodes";
    private static final String ELEM_CATEGORY_LABEL = "Categories";
    private static final String ELEM_IMMEDTRANSACTION_LABEL = "Immediate Transactions";
    private static final int ELEM_MONEYNODE = 0;
    private static final int ELEM_CATEGORY = 1;
    private static final int ELEM_IMMEDTRANSACTION = 2;

    private static Map<String, Integer> elementTypeMapping;
    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat dateTimeFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss");

    static {
        elementTypeMapping = new HashMap<String, Integer>();
        elementTypeMapping.put(ELEM_MONEYNODE_LABEL, ELEM_MONEYNODE);
        elementTypeMapping.put(ELEM_CATEGORY_LABEL, ELEM_CATEGORY);
        elementTypeMapping.put(ELEM_IMMEDTRANSACTION_LABEL, ELEM_IMMEDTRANSACTION);
    }

    @Override
    public void importData(String location, boolean replace) 
        throws ImportException {
        CSVReader reader = null;
        try {
            Log.d("TMM", "Reading CSV from location: " + location);
            reader = new CSVReader(new FileReader(location));

            List<MoneyNode> moneyNodes = new LinkedList<MoneyNode>();
            List<Category> categories = new LinkedList<Category>();

            String[] nextLine;
            Integer currentElementType = ELEM_MONEYNODE;

            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length == 0) {
                    Log.d("TMM", "Found empty line");
                    continue;
                }

                if (nextLine.length == 1) {
                    Log.d("TMM", "Found header line");
                    currentElementType = elementTypeMapping.get(nextLine[0]);
                    reader.readNext(); // Skip headers
                    continue;
                }

                if (currentElementType == null) {
                    throw new ImportException("Unknown element type");
                }

                switch (currentElementType) {
                    case ELEM_MONEYNODE:
                        moneyNodes.add(parseMoneyNode(nextLine, replace));
                        break;
                    case ELEM_CATEGORY:
                        categories.add(parseCategory(nextLine, replace));
                        break;
                    case ELEM_IMMEDTRANSACTION:
                        parseImmedTransaction(nextLine, replace, 
                                moneyNodes, categories);
                        break;
                    default:
                        throw new ImportException("Unknown element type");
                }
            }
        } catch (Exception e) {
            throw new ImportException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new ImportException(e);
                }
            }
        }
    }

    @Override
    public void exportData(String location, Date startDate, Date endDate) 
        throws ExportException {
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(location));

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            List<MoneyNode> moneyNodes = dbHelper.getMoneyNodes();
            List<Category> categories = dbHelper.getCategories();

            List<String> exportOrder = new LinkedList<String>();
            exportOrder.add(ELEM_MONEYNODE_LABEL);
            exportOrder.add(ELEM_CATEGORY_LABEL);
            exportOrder.add(ELEM_IMMEDTRANSACTION_LABEL);

            for (String elementTypeLabel : exportOrder) {
                writer.writeNext(new String[] {elementTypeLabel});
                Integer elementType = elementTypeMapping.get(elementTypeLabel);

                switch (elementType) {
                    case ELEM_MONEYNODE:
                        writeMoneyNodes(writer, moneyNodes);
                        break;
                    case ELEM_CATEGORY:
                        writeCategories(writer, categories);
                        break;
                    case ELEM_IMMEDTRANSACTION:
                        writeImmediateTransactions(writer, moneyNodes, 
                                categories, startDate, endDate);
                        break;
                }
            }
        } catch (Exception e) {
            throw new ExportException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new ExportException(e);
                }
            }
        }
    }

    protected MoneyNode parseMoneyNode(String[] data, boolean replace) 
        throws DatabaseException, ParseException {
        String name = data[0];
        String description = data[1];
        String icon = data[2];
        String currency = data[3];
        Date creationDate = dateFormat.parse(data[4]);
        BigDecimal initialBalance = new BigDecimal(data[5]);

        Log.d("TMM", "Parsed money node");
        Log.d("TMM", "Name: " + name);

        final DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        MoneyNode node = dbHelper.getMoneyNodeWithName(name);

        boolean createNode = true;

        if (node != null) {
            if (replace) {
                node.load();
                node.setDescription(description);
                node.setIcon(icon);
                node.setCurrency(currency);
                node.setCreationDate(creationDate);
                node.setInitialBalance(initialBalance);
                createNode = false;
            } else {
                name = getCopyName(name, new ExistenceChecker() {
                    public boolean exists(String name) 
                        throws DatabaseException {
                        return dbHelper.hasMoneyNodeWithName(name);
                    }
                });
            }
        } 

        if (createNode) {
            Log.d("TMM", "Creating money node with name: " + name);
            node = new MoneyNode(name, description, icon, 
                    creationDate, initialBalance, currency);
        }

        node.save();
        return node;
    }
    
    protected void writeMoneyNodes(CSVWriter writer, 
            List<MoneyNode> moneyNodes) 
        throws DatabaseException {
        writer.writeNext(new String[] {
            "Name",
            "Description",
            "Icon",
            "Currency",
            "Creation Date",
            "Initial Balance"
        });
        for (MoneyNode node : moneyNodes) {
            node.load();
            writer.writeNext(new String[] {
                node.getName(),
                node.getDescription(),
                node.getIcon(),
                node.getCurrency(),
                dateFormat.format(node.getCreationDate()),
                node.getInitialBalance().toString()
            });
        }
    }

    protected void writeCategories(CSVWriter writer, 
            List<Category> categories) 
        throws DatabaseException {
        writer.writeNext(new String[] {
            "Name",
            "Icon"
        });
        for (Category cat : categories) {
            cat.load();
            writer.writeNext(new String[] {
                cat.getName(),
                cat.getIcon(),
            });
        }
    }

    protected void writeImmediateTransactions(CSVWriter writer, 
            List<MoneyNode> moneyNodes,
            List<Category> categories, Date startDate, Date endDate) 
        throws DatabaseException {
        writer.writeNext(new String[] {
            "Money Node",
            "Value",
            "Description",
            "Category",
            "Execution Date"
        });

        int nodeIdx = 0;
        for (MoneyNode node : moneyNodes) {
            for (ImmediateTransaction trans : 
                    node.getImmediateTransactions(startDate, endDate)) {
                trans.load();
                writer.writeNext(new String[] {
                    Integer.toString(nodeIdx),
                    trans.getValue().toString(),
                    trans.getDescription().toString(),
                    Integer.toString(categories.indexOf(trans.getCategory())),
                    dateTimeFormat.format(trans.getExecutionDate())
                });
            }

            nodeIdx++;
        }
    }

    protected Category parseCategory(String[] data, boolean replace)
        throws DatabaseException, ParseException {
        String name = data[0];
        String icon = data[1];

        Log.d("TMM", "Parsed category");
        Log.d("TMM", "Name: " + name);

        final DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Category category = dbHelper.getCategoryWithName(name);

        boolean createCategory = true;

        if (category != null) {
            if (replace) {
                category.load();
                category.setIcon(icon);
                createCategory = false;
            } else {
                name = getCopyName(name, new ExistenceChecker() {
                    public boolean exists(String name) 
                        throws DatabaseException {
                        return dbHelper.hasCategoryWithName(name);
                    }
                });
            }
        }

        if (createCategory) {
            Log.d("TMM", "Creating category with name: " + name);
            category = new Category(name, icon);
        }

        category.save();
        return category;
    }

    protected ImmediateTransaction parseImmedTransaction(String[] data, 
            boolean replace, List<MoneyNode> moneyNodes, 
            List<Category> categories) 
        throws DatabaseException, ParseException {
        Integer moneyNodeIdx = Integer.parseInt(data[0]);
        BigDecimal value = new BigDecimal(data[1]);
        String description = data[2];
        Integer categoryIdx = Integer.parseInt(data[3]);
        Date executionDate = dateTimeFormat.parse(data[4]);

        Log.d("TMM", "Parsed immediate transaction");
        Log.d("TMM", "Value: " + value);

        ImmediateTransaction immedTransaction = null;
        MoneyNode node = moneyNodes.get(moneyNodeIdx);
        Category cat = categories.get(categoryIdx);

        immedTransaction = node.getImmediateTransaction(
                executionDate, value, description, cat);

        if (replace && immedTransaction != null) {
            // Do nothing
            return immedTransaction;
        } else {
            immedTransaction = new ImmediateTransaction(node, value, 
                    description, cat, executionDate);
            immedTransaction.save();
            return immedTransaction;
        }
    }

    protected String getCopyName(String originalName, 
        ExistenceChecker existenceChecker) 
        throws DatabaseException {
        int i = 1;
        String name;

        do {
            StringBuilder nameBuilder = new StringBuilder(originalName);
            nameBuilder.append("(").append(i).append(")");
            name = nameBuilder.toString();
            i++;
        } while (existenceChecker.exists(name));

        return name;
    }

    protected interface ExistenceChecker {
        public boolean exists(String name) throws DatabaseException;
    }
}
