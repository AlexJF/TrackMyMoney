/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.importexport;

import android.content.Context;
import android.net.Uri;
import net.alexjf.tmm.exceptions.ExportException;
import net.alexjf.tmm.exceptions.ImportException;

import java.util.Date;

public abstract class ImportExport {
	public abstract void importData(Context context, Uri location, boolean replace)
			throws ImportException;

	public void exportData(String location) throws ExportException {
		exportData(location, null, null);
	}

	public abstract void exportData(String location, Date startDate, Date endDate)
			throws ExportException;
}
