package io.github.sgpublic.bilidownload.ui.list

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.databinding.ItemLicenseListBinding

class LicenseListAdapter(context: Context, objects: List<LicenseListItem>) : ArrayAdapter<LicenseListAdapter.LicenseListItem>(context, R.layout.item_license_list, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        return if (convertView != null){
            convertView
        } else {
            val itemLicenseListBinding = ItemLicenseListBinding.inflate(LayoutInflater.from(context), parent, false)
            item?.run {
                itemLicenseListBinding.itemLicenseTitle.text = projectTitle
                itemLicenseListBinding.itemLicenseAuthor.text = projectAuthor
                itemLicenseListBinding.itemLicenseAbout.text = projectAbout
                itemLicenseListBinding.itemLicenseBase.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(item.projectUrl)
                    context.startActivity(intent)
                }
            }
            itemLicenseListBinding.root
        }
    }

    data class LicenseListItem(
        val projectTitle: String,
        val projectAbout: String,
        val projectAuthor: String,
        val projectUrl: String
    )
}