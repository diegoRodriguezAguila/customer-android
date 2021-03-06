package com.kustomer.kustomersdk.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kustomer.kustomersdk.Adapters.ImageAttachmentListAdapter;
import com.kustomer.kustomersdk.Helpers.KUSImage;
import com.kustomer.kustomersdk.Helpers.KUSPermission;
import com.kustomer.kustomersdk.Interfaces.KUSInputBarViewListener;
import com.kustomer.kustomersdk.R2;
import com.kustomer.kustomersdk.Utils.KUSUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Junaid on 2/27/2018.
 */

public class KUSInputBarView extends LinearLayout implements TextWatcher, TextView.OnEditorActionListener, ImageAttachmentListAdapter.onItemClickListener {

    //region Properties
    private static final int MAX_BITMAP_PIXELS = 1000000;

    @BindView(R2.id.etTypeMessage) EditText etTypeMessage;
    @BindView(R2.id.btnSendMessage) View btnSendMessage;
    @BindView(R2.id.ivAttachment) ImageView ivAttachment;
    @BindView(R2.id.rvImageAttachment)
    RecyclerView rvImageAttachment;

    KUSInputBarViewListener listener;
    ImageAttachmentListAdapter adapter;
    //endregion

    //region LifeCycle
    public KUSInputBarView(Context context) {
        super(context);
    }

    public KUSInputBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KUSInputBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KUSInputBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        initViews();
        setListeners();
        setupAdapter();
    }
    //endregion

    //region Initializer
    private void initViews(){
        updateSendButton();

        etTypeMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        etTypeMessage.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        KUSUtils.showKeyboard(etTypeMessage,800);
    }

    private void setListeners(){
        etTypeMessage.addTextChangedListener(this);
        etTypeMessage.setOnEditorActionListener(this);
    }

    private void setupAdapter(){
        adapter = new ImageAttachmentListAdapter(this);
        rvImageAttachment.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvImageAttachment.setLayoutManager(layoutManager);

        adapter.notifyDataSetChanged();
    }
    //endregion

    //region Public Methods
    public void setListener(KUSInputBarViewListener listener){
        this.listener = listener;
    }

    public void setText(String text){
        etTypeMessage.setText(text.trim());
    }

    public String getText(){
        return etTypeMessage.getText().toString().trim();
    }

    public void setAllowsAttachment(boolean allowAttachment){
        if(!allowAttachment)
            ivAttachment.setVisibility(GONE);
        else{
            boolean shouldBeHidden = !KUSPermission.isCameraPermissionDeclared(getContext())
                    && !KUSPermission.isReadPermissionDeclared(getContext());

            ivAttachment.setVisibility(shouldBeHidden ? GONE : VISIBLE);
        }
    }

    public void removeAllAttachments(){
        adapter.removeAll();
    }

    public void attachImage(String imageUri){
        adapter.attachImage(imageUri);

        if(adapter.getItemCount() == 1)
            rvImageAttachment.setVisibility(VISIBLE);

        rvImageAttachment.scrollToPosition(adapter.getItemCount()-1);
    }

    public List<Bitmap> getAllImages(){

        List<String> imageURIs = new ArrayList<>(adapter.getImageURIs());
        if(imageURIs.size() != 0) {
            List<Bitmap> images = new ArrayList<>();

            for (String uri : imageURIs) {
                if (!uri.startsWith("content")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(Uri.parse(uri).getPath());

                    try {
                        bitmap = KUSImage.rotateBitmapIfNeeded(bitmap,getContext().getContentResolver().openInputStream(Uri.parse(uri)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (bitmap != null)
                        images.add(KUSImage.getScaledImage(bitmap, MAX_BITMAP_PIXELS));

                    bitmap = null;
                } else {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.parse(uri));

                        try {
                            bitmap = KUSImage.rotateBitmapIfNeeded(bitmap,getContext().getContentResolver().openInputStream(Uri.parse(uri)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (bitmap != null)
                            images.add(KUSImage.getScaledImage(bitmap, MAX_BITMAP_PIXELS));

                        bitmap = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return images;
        }else
            return null;
    }

    public void requestInputFocus(){
        etTypeMessage.requestFocus();
    }

    public void clearInputFocus(){
        etTypeMessage.clearFocus();
        KUSUtils.hideKeyboard(this);
    }
    //endregion

    //region Interface element methods
    @OnClick(R2.id.ivAttachment)
    void attachmentClicked(){
        if(listener != null)
            listener.inputBarAttachmentClicked();
    }

    @OnClick(R2.id.btnSendMessage)
    void sendPressed(){
        String text = getText();
        if(text.length() == 0)
            return;

        if(listener != null)
            listener.inputBarSendClicked();
    }

    private void updateSendButton(){
        String text = getText();
        boolean shouldEnableSend = text.length() > 0;

        if(listener != null)
            shouldEnableSend = listener.inputBarShouldEnableSend();

        btnSendMessage.setEnabled(shouldEnableSend);
        btnSendMessage.setAlpha(shouldEnableSend ? 1.0f : 0.5f);
    }
    //endregion

    //region Listeners
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        updateSendButton();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if(i == EditorInfo.IME_ACTION_SEND){
            sendPressed();
        }
        return false;
    }

    @Override
    public void onAttachmentImageClicked(int position, List<String> imageURIs) {
        new KUSLargeImageViewer(getContext()).showImages(imageURIs,position);
    }

    @Override
    public void onAttachmentImageRemoved() {
        if(adapter.getItemCount() == 0)
            rvImageAttachment.setVisibility(GONE);
    }
    //endregion
}
