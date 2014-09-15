/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import net.alexjf.tmm.database.DatabaseManager;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.exceptions.DbObjectLoadException;
import net.alexjf.tmm.exceptions.DbObjectSaveException;
import net.alexjf.tmm.utils.Cache;
import net.alexjf.tmm.utils.CacheFactory;
import net.sqlcipher.database.SQLiteDatabase;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a single user of the application.
 */
public class MoneyNode extends DatabaseObject {
	// Intent keys
	public static final String KEY_MONEYNODE = "curMoneyNode";
	public static final String KEY_CURRENCY = "currency";

	// Database tables
	public static final String TABLE_NAME = "MoneyNodes";

	// Table columns
	public static final String COL_ID = "id";
	public static final String COL_NAME = "name";
	public static final String COL_DESCRIPTION = "description";
	public static final String COL_ICON = "icon";
	public static final String COL_CURRENCY = "currency";
	public static final String COL_CREATIONDATE = "creationDate";
	public static final String COL_INITIALBALANCE = "initialBalance";

	// Schema
	private static final String SCHEMA_CREATETABLE =
			"CREATE TABLE " + TABLE_NAME + " (" +
					COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
					COL_NAME + " TEXT NOT NULL," +
					COL_DESCRIPTION + " TEXT," +
					COL_ICON + " TEXT," +
					COL_CURRENCY + " TEXT," +
					COL_CREATIONDATE + " DATETIME " +
					"DEFAULT (DATETIME('now', 'localtime'))," +
					COL_INITIALBALANCE + " TEXT DEFAULT 0" +
					");";
	private static final String SCHEMA_TRANSFER_TO_TMP =
			"INSERT INTO " + TABLE_NAME + "_tmp SELECT * FROM " + TABLE_NAME + ";";
	private static final String SCHEMA_FK_DISABLE =
			"PRAGMA foreign_keys=OFF;";
	private static final String SCHEMA_DROP_TABLE =
			"DROP TABLE " + TABLE_NAME + ";";
	private static final String SCHEMA_TMP_TO_NEW =
			"ALTER TABLE " + TABLE_NAME + "_tmp RENAME TO " + TABLE_NAME + ";";
	private static final String SCHEMA_FK_CHECK =
			"PRAGMA foreign_key_check;";
	private static final String SCHEMA_FK_ENABLE =
			"PRAGMA foreign_keys=ON;";

	// Queries
	private static final String QUERY_BALANCE =
			"SELECT " + Transaction.COL_VALUE +
					" FROM " + Transaction.TABLE_NAME +
					"  INNER JOIN " + ImmediateTransaction.TABLE_NAME +
					"  USING (" + Transaction.COL_ID + ") " +
					" WHERE " + Transaction.COL_MONEYNODEID + " = ?";
	private static final String QUERY_IMMEDIATETRANSACTIONS =
			"SELECT " + ImmediateTransaction.COL_ID +
					" FROM " + Transaction.TABLE_NAME +
					"  INNER JOIN " + ImmediateTransaction.TABLE_NAME +
					"  USING (" + Transaction.COL_ID + ") " +
					" WHERE " + Transaction.COL_MONEYNODEID + " = ?";

	// Database maintenance

	/**
	 * Creates schemas associated with a MoneyNode domain.
	 *
	 * @param db Database where to create the schemas.
	 */
	public static void onDatabaseCreation(SQLiteDatabase db) {
		db.execSQL(SCHEMA_CREATETABLE);
	}

	/**
	 * Updates schemas associated with a Category domain.
	 *
	 * @param db         Database where to update the schemas.
	 * @param oldVersion The old version of the schemas.
	 * @param newVersion The new version of the schemas.
	 */
	public static void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		switch (oldVersion) {
			case 0:
			case 1:
			case 2:
				db.execSQL(SCHEMA_FK_DISABLE);
				db.execSQL(SCHEMA_CREATETABLE.replaceFirst(TABLE_NAME, TABLE_NAME + "_tmp"));
				db.execSQL(SCHEMA_TRANSFER_TO_TMP);
				db.execSQL(SCHEMA_DROP_TABLE);
				db.execSQL(SCHEMA_TMP_TO_NEW);
				db.execSQL(SCHEMA_FK_CHECK);
				db.execSQL(SCHEMA_FK_ENABLE);
				break;
		}
	}

	// Caching
	private static Cache<Long, MoneyNode> cache = CacheFactory.getInstance().getCache("MoneyNode");

	public static List<MoneyNode> getMoneyNodes() throws DatabaseException {
		SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();

		Cursor cursor = db.query(MoneyNode.TABLE_NAME,
				new String[]{MoneyNode.COL_ID},
				null, null, null, null, null, null);

		List<MoneyNode> moneyNodes = new ArrayList<MoneyNode>(cursor.getCount());

		while (cursor.moveToNext()) {
			MoneyNode moneyNode = MoneyNode.createFromId(cursor.getLong(0));
			moneyNode.setDb(db);
			moneyNodes.add(moneyNode);
		}

		cursor.close();

		return moneyNodes;
	}

	public static MoneyNode getMoneyNodeWithName(String name) throws DatabaseException {
		SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
		Long id = null;

		Cursor cursor = db.query(MoneyNode.TABLE_NAME,
				new String[]{MoneyNode.COL_ID},
				MoneyNode.COL_NAME + " = ?",
				new String[]{name},
				null, null, null, null);

		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			id = cursor.getLong(0);
		}

		cursor.close();

		return MoneyNode.createFromId(id);
	}

	public static boolean hasMoneyNodeWithName(String name) throws DatabaseException {
		return getMoneyNodeWithName(name) != null;
	}

	/*
	 * TODO: On money node deletion, have the option to move the transactions
	 * 		 to another money node.
	 */
	public static void deleteMoneyNode(MoneyNode node) throws DatabaseException {
		if (node == null) {
			return;
		}

		SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();

		db.delete(MoneyNode.TABLE_NAME, MoneyNode.COL_ID + " = ?",
				new String[]{node.getId().toString()});
	}


	/**
	 * Gets an instance of MoneyNode with the specified id.
	 * <p/>
	 * The instance is reused from the cache or created if it doesn't exist on
	 * the cache yet.
	 *
	 * @param id The id of the moneynode we want.
	 * @return MoneyNode instance with specified id.
	 */
	public static MoneyNode createFromId(Long id) {
		if (id == null) {
			return null;
		}

		MoneyNode node = cache.get(id);

		if (node == null) {
			node = new MoneyNode(id);
			cache.put(id, node);
		}

		return node;
	}

	// Private members
	private String name;
	private String description;
	private String icon;
	private CurrencyUnit currency;
	private Date creationDate;
	private Money initialBalance;
	private Money balance;

	private MoneyNode(Long id) {
		setId(id);
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param name           The name for this instance.
	 * @param description    The description for this instance.
	 * @param icon           The icon drawable entry name for this instance.
	 * @param creationDate   The creationDate for this instance.
	 * @param initialBalance The initialBalance for this instance.
	 * @param currency       The currency for this instance.
	 */
	public MoneyNode(String name, String description,
			String icon, Date creationDate, Money initialBalance,
			CurrencyUnit currency) {
		this.name = name;
		this.description = description;
		this.icon = icon;
		this.currency = currency;
		this.creationDate = creationDate;
		this.initialBalance = initialBalance;
		this.balance = initialBalance;
		setChanged(true);
	}

	@Override
	protected void internalLoad() throws DbObjectLoadException {
		Cursor cursor = getDb().query(TABLE_NAME, null, COL_ID + " = ?",
				new String[]{getId().toString()}, null, null, null, null);

		try {
			if (cursor.moveToFirst()) {
				name = cursor.getString(1);
				description = cursor.getString(2);
				icon = cursor.getString(3);
				currency = CurrencyUnit.getInstance(cursor.getString(4));
				creationDate = new Date(cursor.getLong(5));
				initialBalance = Money.of(currency, new BigDecimal(cursor.getString(6)));
				// Update balance
				getBalance();
			} else {
				throw new DbObjectLoadException("Couldn't find money node" +
						"associated with id " + getId());
			}
		} finally {
			cursor.close();
		}
	}

	@Override
	protected long internalSave() throws DbObjectSaveException {
		Long id = getId();

		ContentValues contentValues = new ContentValues();
		contentValues.put(COL_ID, id);
		contentValues.put(COL_NAME, name);
		contentValues.put(COL_DESCRIPTION, description);
		contentValues.put(COL_ICON, icon);
		contentValues.put(COL_CURRENCY, currency.getCurrencyCode());
		contentValues.put(COL_CREATIONDATE, creationDate.getTime());
		contentValues.put(COL_INITIALBALANCE, initialBalance.getAmount().toString());
		Log.d("TMM", "Money Node - Save initial balance: " + initialBalance);

		long result;

		if (id != null) {
			result = getDb().update(TABLE_NAME, contentValues,
					COL_ID + " = ?", new String[]{id.toString()});
			result = (result == 0 ? -1 : id);
		} else {
			result = getDb().insert(TABLE_NAME, null, contentValues);
			cache.put(result, this);
		}

		if (result >= 0) {
			cache.put(result, this);
			return result;
		} else {
			throw new DbObjectSaveException("Error saving moneynode to database");
		}
	}

	/**
	 * Sets the name for this instance.
	 *
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
		setChanged(true);
	}

	/**
	 * Sets the description for this instance.
	 *
	 * @param description The description.
	 */
	public void setDescription(String description) {
		this.description = description;
		setChanged(true);
	}

	/**
	 * Sets the icon drawable entry name for this instance.
	 *
	 * @param icon The icon drawable entry name.
	 */
	public void setIcon(String icon) {
		this.icon = icon;
		setChanged(true);
	}

	/**
	 * Sets the currency for this instance.
	 *
	 * @param currency The currency.
	 */
	public void setCurrency(CurrencyUnit currency) {
		this.currency = currency;
		setChanged(true);
	}

	/**
	 * Sets the creationDate for this instance.
	 *
	 * @param creationDate The creationDate.
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
		setChanged(true);
	}

	/**
	 * Sets the initialBalance for this instance.
	 *
	 * @param initialBalance The initialBalance.
	 */
	public void setInitialBalance(Money initialBalance) {
		this.initialBalance = initialBalance;
		this.balance = null;
		setChanged(true);
	}

	/**
	 * Gets the name for this instance.
	 *
	 * @return The name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the description for this instance.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Gets the icon drawable entry name for this instance.
	 *
	 * @return The location.
	 */
	public String getIcon() {
		return this.icon;
	}

	/**
	 * Gets the currency for this instance.
	 *
	 * @return The currency.
	 */
	public CurrencyUnit getCurrency() {
		return this.currency;
	}

	/**
	 * Gets the creationDate for this instance.
	 *
	 * @return The creationDate.
	 */
	public Date getCreationDate() {
		return this.creationDate;
	}

	/**
	 * Gets the initialBalance for this instance.
	 *
	 * @return The initialBalance.
	 */
	public Money getInitialBalance() {
		return this.initialBalance;
	}

	/**
	 * Gets the balance for this instance.
	 *
	 * @return The balance.
	 */
	public Money getBalance() {
		if (balance == null) {
			try {
				updateBalance();
			} catch (DatabaseException e) {
				Log.e("TMM", "Unable to get money node's balance", e);
			}
		}

		return balance;
	}

	public ImmediateTransaction getImmediateTransaction(Date executionDate,
			Money value, String description, Category category)
			throws DatabaseException {
		dbReadyOrThrow();

		SQLiteDatabase db = getDb();
		Long id = null;

		StringBuilder queryBuilder =
				new StringBuilder(QUERY_IMMEDIATETRANSACTIONS);
		queryBuilder.append(" AND ");
		queryBuilder.append(ImmediateTransaction.COL_EXECUTIONDATE);
		queryBuilder.append(" = ? AND ");
		queryBuilder.append(ImmediateTransaction.COL_VALUE);
		queryBuilder.append(" = ? AND ");
		queryBuilder.append(ImmediateTransaction.COL_DESCRIPTION);
		queryBuilder.append(" = ? AND ");
		queryBuilder.append(ImmediateTransaction.COL_CATEGORYID);
		queryBuilder.append(" = ?");

		Cursor cursor = db.rawQuery(queryBuilder.toString(),
				new String[]{
						getId().toString(),
						Long.toString(executionDate.getTime()),
						value.getAmount().toString(),
						description,
						category.getId().toString()
				});

		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			id = cursor.getLong(0);
		}

		cursor.close();

		return ImmediateTransaction.createFromId(id);
	}

	public List<ImmediateTransaction> getImmediateTransactions()
			throws DatabaseException {
		return getImmediateTransactions(null, null);
	}

	/**
	 * Retrieves a list of immediate transactions associated with this money
	 * node between the date interval specified.
	 *
	 * @param startDate Starting date of interval or null for no limit.
	 * @param endDate   Ending date of interval or null for no limit.
	 * @return List of ImmediateTransactions executed inside this interval and
	 * associated with this money node.
	 */
	public List<ImmediateTransaction> getImmediateTransactions(Date startDate,
			Date endDate) throws DatabaseException {
		dbReadyOrThrow();

		SQLiteDatabase db = getDb();

		List<ImmediateTransaction> immediateTransactions =
				new LinkedList<ImmediateTransaction>();

		String query = QUERY_IMMEDIATETRANSACTIONS;

		if (startDate != null) {
			query += " AND " + ImmediateTransaction.COL_EXECUTIONDATE +
					" >= '" + startDate.getTime() + "'";
		}

		if (endDate != null) {
			query += " AND " + ImmediateTransaction.COL_EXECUTIONDATE +
					" <= '" + endDate.getTime() + "'";
		}

		Log.d("TMM", "ImmediateTransactin query: \n" + query);

		Cursor cursor = db.rawQuery(query,
				new String[]{getId().toString()});

		while (cursor.moveToNext()) {
			ImmediateTransaction immediateTransaction =
					ImmediateTransaction.createFromId(cursor.getLong(0));
			immediateTransaction.setDb(db);
			immediateTransactions.add(immediateTransaction);
		}

		cursor.close();

		return immediateTransactions;
	}

	/*
	 * TODO: When removing an immediate transaction associated with a transfer,
	 *       give the option of deleting the linked transaction as well
	 *       (currently, a deletion will simply remove that connection and the
	 *       other transaction will continue to exist).
	 */
	public void removeImmediateTransaction(ImmediateTransaction transaction)
			throws DatabaseException {
		if (transaction == null) {
			return;
		}

		ImmediateTransaction transferTransaction = transaction.getTransferTransaction();

		if (transferTransaction != null) {
			transferTransaction.load();
		}

		dbReadyOrThrow();

		SQLiteDatabase db = getDb();

		int result = db.delete(Transaction.TABLE_NAME,
				Transaction.COL_ID + " = ?",
				new String[]{transaction.getId().toString()});

		if (result == 1) {
			notifyBalanceChange(transaction.getValue().negated());

			// Since other transaction in transfer is deleted by sqlite due
			// to cascade, notify other money node of updated balance
			if (transferTransaction != null) {
				transferTransaction.getMoneyNode().notifyBalanceChange(
						transaction.getValue());
			}
		}
	}

	public String toString() {
		return getName();
	}

	public static final Parcelable.Creator<MoneyNode> CREATOR =
			new Parcelable.Creator<MoneyNode>() {
				public MoneyNode createFromParcel(Parcel in) {
					Long id = in.readLong();
					return createFromId(id);
				}

				public MoneyNode[] newArray(int size) {
					return new MoneyNode[size];
				}
			};

	private void updateBalance()
			throws DatabaseException {
		dbReadyOrThrow();

		SQLiteDatabase db = getDb();

		Cursor cursor = db.rawQuery(QUERY_BALANCE,
				new String[]{getId().toString()});

		balance = initialBalance;

		while (cursor.moveToNext()) {
			balance = Money.of(currency, new BigDecimal(cursor.getString(0))).plus(balance);
		}

		cursor.close();
	}

	void notifyBalanceChange(Money delta) {
		if (balance == null || delta == null) {
			return;
		}

		balance = balance.plus(delta);
	}
}
