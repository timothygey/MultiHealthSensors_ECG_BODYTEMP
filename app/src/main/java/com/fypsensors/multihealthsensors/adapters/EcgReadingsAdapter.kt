package com.fypsensors.multihealthsensors.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fypsensors.multihealthsensors.R
import com.fypsensors.multihealthsensors.databinding.CustomReadingsLayoutBinding
import com.fypsensors.multihealthsensors.models.EcgReadings
import com.fypsensors.multihealthsensors.models.Readings
import java.text.SimpleDateFormat
import java.util.*


class EcgReadingsAdapter(
    private val context: Context,
    private val readingsArray: ArrayList<EcgReadings>,
) :
    RecyclerView.Adapter<EcgReadingsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            CustomReadingsLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )


        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val readings: EcgReadings = readingsArray[i]

        viewHolder.bind(readings)

    }

    override fun getItemCount(): Int {

        return 30

    }

    inner class ViewHolder(private val binding: CustomReadingsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {


        init {


        }

        @SuppressLint("SetTextI18n")
        fun bind(readings: EcgReadings) {
            binding.apply {
                if (adapterPosition % 2 == 1) {
                    binding.temperatureTv.setBackgroundColor(context.resources.getColor(R.color.grey))
                    binding.timestampTv.setBackgroundColor(context.resources.getColor(R.color.grey))

                } else {
                    binding.temperatureTv.setBackgroundColor(context.resources.getColor(R.color.white))
                    binding.timestampTv.setBackgroundColor(context.resources.getColor(R.color.white))

                }
                binding.timestampTv.text = readings.bpm_dt
                binding.temperatureTv.text = readings.bpm.toString()


            }
        }


    }


}

