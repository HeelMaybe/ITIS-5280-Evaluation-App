package com.auth0.samples;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.auth0.samples.databinding.ListPosterBinding;

import java.util.List;

public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {

    private List<Poster> posters;
    private IAdapter mlistener;

    public PosterAdapter(List<Poster> myDataset, IAdapter listener){
        this.posters = myDataset;
        this.mlistener = listener;
    }

    public static class PosterViewHolder extends RecyclerView.ViewHolder{
        public Context context;
        public ListPosterBinding binding;
        public PosterViewHolder(ListPosterBinding listPosterBinding){
            super(listPosterBinding.getRoot());
            context = itemView.getContext();
            binding = listPosterBinding;
        }
    }
    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListPosterBinding binding = ListPosterBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PosterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        Poster poster = posters.get(position);
        holder.binding.posterTitle.setText("Title: "+ poster.getTitle());
        holder.binding.posterListParticipants.setText(poster.getParticipants());
        holder.binding.itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mlistener.goToEvaluation(poster);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posters.size();
    }

    interface IAdapter {
        void goToEvaluation(Poster poster);
    }
}
