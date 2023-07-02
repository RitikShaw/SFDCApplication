package com.example.majorprojectui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.majorprojectui.Models.MedicinesData
import com.example.majorprojectui.databinding.ActivityUserProfileBinding
import com.example.majorprojectui.databinding.LayoutRvitemMedicinesBinding

class DiseaseMedsAdapter (val medicineDataList : ArrayList<MedicinesData>) : RecyclerView.Adapter<DiseaseMedsAdapter.ViewHolder>() {
    class ViewHolder(val binding: LayoutRvitemMedicinesBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutRvitemMedicinesBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return medicineDataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            tvIndex.text = "${position+1}"
            tvMedicines.text = medicineDataList[position].medicinesName
            tvMedicines.setOnClickListener {
                holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(medicineDataList[position].medicinesLinks)))
            }
        }
    }
}