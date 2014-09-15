/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.interfaces;

import android.widget.BaseAdapter;

public interface IWithAdapter {
	public BaseAdapter getAdapter();

	public void setAdapter(BaseAdapter adapter);
}
