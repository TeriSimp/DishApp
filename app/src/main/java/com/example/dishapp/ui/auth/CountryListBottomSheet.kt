package com.example.dishapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dishapp.R
import com.example.dishapp.models.Country
import com.example.dishapp.models.CountryData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CountryListBottomSheet(
    private val onSelect: (dialCode: String, iso2: String) -> Unit
) : BottomSheetDialogFragment() {

    private val countries: List<Country> = CountryData.list

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_country_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvCountries)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = CountryAdapter(countries) { country ->
            onSelect(country.dialCode, country.iso2)
            dismiss()
        }
    }

    private class CountryAdapter(
        private val list: List<Country>,
        private val itemClick: (Country) -> Unit
    ) : RecyclerView.Adapter<CountryAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_country, parent, false)
            return VH(v, itemClick)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(list[position])
        }

        class VH(itemView: View, private val click: (Country) -> Unit) :
            RecyclerView.ViewHolder(itemView) {

            private val tvFlag: TextView = itemView.findViewById(R.id.tvFlag)
            private val tvName: TextView = itemView.findViewById(R.id.tvCountryName)
            private val tvDial: TextView = itemView.findViewById(R.id.tvDialCode)

            fun bind(c: Country) {
                tvFlag.text = c.flagEmoji
                tvName.text = c.name
                tvDial.text = c.dialCode
                itemView.setOnClickListener { click(c) }
            }
        }
    }
}
