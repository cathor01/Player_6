package com.cathor.n_6

import android.view.View
import com.gordonwong.materialsheetfab.AnimatedFab
import com.gordonwong.materialsheetfab.MaterialSheetFab

/**
 * Created by Cathor on 2016/3/2 15:37.
 */

class MyMaterialSheetFab : MaterialSheetFab<Fabs> {
    constructor(fab: Fabs, sheet: View, overlay: View, sheetColor:Int, fabColor: Int,
                onClickListener: ((View)->Unit)?):
    super(fab, sheet, overlay, sheetColor, fabColor){
        fab.setOnClickListener(onClickListener)
        fab.setOnLongClickListener {
            showSheet()
            true
        }
    }
}
