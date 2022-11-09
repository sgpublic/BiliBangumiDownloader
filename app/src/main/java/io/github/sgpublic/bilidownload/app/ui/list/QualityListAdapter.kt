package io.github.sgpublic.bilidownload.app.ui.list

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.github.sgpublic.bilidownload.base.ui.SelectableArrayAdapter
import io.github.sgpublic.bilidownload.base.ui.SingleSelection
import io.github.sgpublic.bilidownload.core.exsp.BangumiPreference
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.databinding.ItemQualityListBinding
import io.github.sgpublic.exsp.ExPreference

class QualityListAdapter : SelectableArrayAdapter<ItemQualityListBinding, Map.Entry<Int, String>>(),
    SingleSelection<Map.Entry<Int, String>>{
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemQualityListBinding.inflate(inflater, parent, false)

    private val BangumiPreference: BangumiPreference by lazy { ExPreference.get() }
    override fun setOnItemClickListener(onClick: (Map.Entry<Int, String>) -> Unit) {
        super.setOnItemClickListener {
            if (BangumiPreference.quality != it.key) {
                onClick.invoke(it)
            }
        }
    }

    override fun onBindViewHolder(
        context: Context,
        ViewBinding: ItemQualityListBinding,
        data: Map.Entry<Int, String>
    ) {
        ViewBinding.root.text = data.value
        ViewBinding.root.gravity = Gravity.CENTER_HORIZONTAL
        ViewBinding.root.setTextColor(ContextCompat.getColor(
            context, (getSelection() == data.key).take(getSelectedColor(), getNormalColor())
        ))
    }

    override fun getClickableView(ViewBinding: ItemQualityListBinding) = ViewBinding.root
    override val Adapter: SelectableArrayAdapter<*, Map.Entry<Int, String>> = this
    override var position: Int = 0
}