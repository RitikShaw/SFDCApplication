package com.example.majorprojectui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.majorprojectui.databinding.RvItemImguploadBinding

class ContributionAdapter(
    private val imageUriList : MutableList<Uri?>,
) : RecyclerView.Adapter<ContributionAdapter.ViewHolder>() {
    class ViewHolder(val binding : RvItemImguploadBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvItemImguploadBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return imageUriList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            imvImage.setImageURI(imageUriList[position])
        }
    }
}