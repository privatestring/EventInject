package com.joker.event.launcher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.webull.commonmodule.comment.ideas.viewmodel.PostItemViewModel;
import launcher.Boom;

public class FeedDetailPopDialog extends DialogFragment {
    @Boom(index = 0)
    PostItemViewModel mPostItemViewModel;
    @Boom(index = 1)
    String mPostContent;
    @Boom(index = 2, isOptional = true)
    int position = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FeedDetailPopDialogLauncher.bind(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
