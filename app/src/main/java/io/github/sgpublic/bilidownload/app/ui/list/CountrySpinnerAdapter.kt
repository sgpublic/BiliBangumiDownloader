package io.github.sgpublic.bilidownload.app.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.forest.data.CountryResp
import io.github.sgpublic.bilidownload.databinding.ItemCountryListBinding
import io.github.sgpublic.bilidownload.databinding.ItemCountrySelectedBinding

class CountrySpinnerAdapter(
    private val countryList: ArrayList<Map.Entry<Int, CountryResp.CountryItem>>,
): BaseAdapter() {
    override fun getCount(): Int = countryList.size
    override fun getItem(pos: Int): Map.Entry<Int, CountryResp.CountryItem> = countryList[pos]
    override fun getItemId(pos: Int): Long = getItem(pos).key.toLong()

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val binding: ItemCountryListBinding = if (convertView != null) {
            ItemCountryListBinding.bind(convertView)
        } else {
            ItemCountryListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        }
        binding.root.text = binding.root.context.getString(R.string.text_login_country, item.value.cname, item.value.countryId)
        return binding.root
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val binding: ItemCountrySelectedBinding = if (convertView != null) {
            ItemCountrySelectedBinding.bind(convertView)
        } else {
            ItemCountrySelectedBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        }
        binding.root.text = binding.root.context.getString(R.string.text_login_country_selected, item.value.countryId)
        return binding.root
    }
}