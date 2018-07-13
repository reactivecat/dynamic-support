/*
 * Copyright 2018 Pranav Pandey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pranavpandey.android.dynamic.support.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.pranavpandey.android.dynamic.support.R;
import com.pranavpandey.android.dynamic.support.preference.DynamicPreferences;
import com.pranavpandey.android.dynamic.support.utils.DynamicResourceUtils;

/**
 * Base preference to provide the basic interface for the extending
 * preference with a icon, title, summary, description, value and an
 * action button.
 * <p>
 * It must be extended and the necessary methods should be implemented to
 * create a dynamic preference.</p>
 *
 * @see DynamicSimplePreference
 * @see DynamicScreenPreference
 * @see DynamicCheckPreference
 * @see DynamicImagePreference
 * @see DynamicSpinnerPreference
 * @see DynamicSeekBarPreference
 * @see DynamicColorPreference
 */
public abstract class DynamicPreference extends FrameLayout
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Listener to get various callbacks related to the popup and
     * dialog. It will be useful if this preference is displaying a
     * popup or dialog and we have to restrict it from doing that.
     * <p>
     * Most possible situation is if we want to display the color
     * picker dialog only if user has purchased this feature.</p>
     *
     * @see DynamicColorPreference
     * @see DynamicSpinnerPreference
     */
    public interface OnPromptListener {

        /**
         * @return {@code true} to allow this preference to show the
         * corresponding popup.
         */
        boolean onPopup();

        /**
         * This method will be called when a popup item is clicked.
         *
         * @param parent The adapter view displaying the items.
         * @param view The item view which has been clicked.
         * @param position The position of {@code view} inside the
         *                 {@code parent}.
         * @param id The id of the {@code view}.
         */
        void onPopupItemClick(@Nullable AdapterView<?> parent, @Nullable View view,
                              int position, long id);

        /**
         * @return {@code true} to allow this preference to show the
         * corresponding dialog.
         */
        boolean onDialog();
    }

    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Icon used by this preference.
     */
    private Drawable mIcon;

    /**
     * Title used by this preference.
     */
    private CharSequence mTitle;

    /**
     * Summary used by this preference.
     */
    private CharSequence mSummary;

    /**
     * Description used by this preference.
     */
    private CharSequence mDescription;

    /**
     * Value string used by this preference.
     */
    private CharSequence mValueString;

    /**
     * Shared preferences key for this preference.
     */
    private String mPreferenceKey;

    /**
     * Shared preferences key on which this preference is
     * dependent.
     */
    private String mDependency;

    /**
     * {@code true} if this preference is enabled and can accept
     * click events.
     */
    private boolean mEnabled;

    /**
     * Action string used by this preference.
     */
    private CharSequence mActionString;

    /**
     * On click listener to receive preference click events.
     */
    private View.OnClickListener mOnPreferenceClickListener;

    /**
     * On click listener to receive action click events.
     */
    private View.OnClickListener mOnActionClickListener;

    /**
     * Listener to get various callbacks related to the popup and
     * dialog. It will be useful if this preference is displaying a
     * popup or dialog and we have to restrict it from doing that.
     */
    private OnPromptListener mOnPromptListener;

    public DynamicPreference(@NonNull Context context) {
        this(context, null);
    }

    public DynamicPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        loadFromAttributes(attrs);
    }

    public DynamicPreference(@NonNull Context context,
                             @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        loadFromAttributes(attrs);
    }

    /**
     * Load values from the supplied attribute set.
     *
     * @param attrs The supplied attribute set to load the values.
     */
    private void loadFromAttributes(@Nullable AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DynamicPreference);

        try {
            mIcon = DynamicResourceUtils.getDrawable(getContext(), a.getResourceId(
                    R.styleable.DynamicPreference_ads_dynamicPreference_icon,
                    DynamicResourceUtils.ADS_DEFAULT_RESOURCE_VALUE));
            mTitle = a.getString(R.styleable.DynamicPreference_ads_dynamicPreference_title);
            mSummary = a.getString(R.styleable.DynamicPreference_ads_dynamicPreference_summary);
            mDescription = a.getString(R.styleable.DynamicPreference_ads_dynamicPreference_description);
            mValueString = a.getString(R.styleable.DynamicPreference_ads_dynamicPreference_value);
            mPreferenceKey = a.getString(R.styleable.DynamicPreference_ads_dynamicPreference_key);
            mDependency = a.getString(R.styleable.DynamicPreference_ads_dynamicPreference_dependency);
            mEnabled = a.getBoolean(R.styleable.DynamicPreference_ads_dynamicPreference_enabled, DEFAULT_ENABLED);
        } finally {
            a.recycle();
        }

        onLoadAttributes(attrs);
        onInflate();
        onUpdate();
        setEnabled(mEnabled);
        updateDependency();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Load values from the supplied attribute set.
     *
     * @param attrs The attribute set to load the values.
     */
    protected abstract void onLoadAttributes(@Nullable AttributeSet attrs);

    /**
     * @return The layout resource id for this preference.
     */
    protected abstract @LayoutRes int getLayoutRes();

    /**
     * This method will be called after loading the attributed.
     * Initialize the preference layout here.
     */
    protected abstract void onInflate();

    /**
     * This method will be called whenever there is a change in the
     * preference attributes or parameters. It is better to do any
     * real time calculation like updating the value string or checked
     * state in this method.
     */
    protected abstract void onUpdate();

    /**
     * This method will be called whenever there is a change in
     * the preference view state. Either {@code enabled} or
     * {@code disabled}, preference views like icon, title, value,
     * etc. must be updated here to reflect the overall preference
     * state.
     *
     * @param enabled {@code true} if this widget is enabled and
     *                can receive click events.
     */
    protected abstract void onEnabled(boolean enabled);

    /**
     * Update this preference according to the dependency.
     */
    private void updateDependency() {
        if (mDependency != null) {
            setEnabled(DynamicPreferences.getInstance()
                    .loadPrefs(mDependency, isEnabled()));
        }
    }

    /**
     * @return The icon used by this preference.
     */
    public @Nullable Drawable getIcon() {
        return mIcon;
    }

    /**
     * Set the icon used by this preference.
     *
     * @param icon The icon drawable to be set.
     */
    public void setIcon(@Nullable Drawable icon) {
        this.mIcon = icon;

        onUpdate();
    }

    /**
     * @return The title used by this preference.
     */
    public @Nullable CharSequence getTitle() {
        return mTitle;
    }

    /**
     * Set the title used by this preference.
     *
     * @param title The title to be set.
     */
    public void setTitle(@Nullable String title) {
        this.mTitle = title;

        onUpdate();
    }

    /**
     * @return The summary used by this preference.
     */
    public @Nullable CharSequence getSummary() {
        return mSummary;
    }

    /**
     * Set the summary used by this preference.
     *
     * @param summary The summary to be set.
     */
    public void setSummary(@Nullable String summary) {
        this.mSummary = summary;

        onUpdate();
    }

    /**
     * @return The description used by this preference.
     */
    public @Nullable CharSequence getDescription() {
        return mDescription;
    }

    /**
     * Set the description used by this preference.
     *
     * @param description The description to be set.
     */
    public void setDescription(@Nullable String description) {
        this.mDescription = description;

        onUpdate();
    }

    /**
     * @return The value string used by this preference.
     */
    public @Nullable CharSequence getValueString() {
        return mValueString;
    }

    /**
     * Set the value string used by this preference.
     *
     * @param valueString The value string to be set.
     */
    public void setValueString(@Nullable CharSequence valueString) {
        this.mValueString = valueString;

        onUpdate();
    }

    /**
     * @return The shared preferences key used by this preference.
     */
    public @Nullable String getPreferenceKey() {
        return mPreferenceKey;
    }

    /**
     * Set the shared preferences key used by this preference.
     *
     * @param preferenceKey The shared preferences key to be set.
     */
    public void setPreferenceKey(@Nullable String preferenceKey) {
        this.mPreferenceKey = preferenceKey;

        onUpdate();
    }

    /**
     * @return The shared preferences key on which this preference
     *         is dependent.
     */
    public @Nullable String getDependency() {
        return mDependency;
    }

    /**
     * Set the shared preferences key on which this preference
     * is dependent.
     *
     * @param dependency The shared preferences key to be set.
     */
    public void setDependency(@Nullable String dependency) {
        this.mDependency = dependency;

        updateDependency();
    }

    /**
     * Set this preference enabled or disabled.
     *
     * @param enabled {@code true} if this preference
     *                is enabled.
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        this.mEnabled = enabled;
        onEnabled(enabled);
    }

    /**
     * @return The action string used by this preference.
     */
    public @Nullable CharSequence getActionString() {
        return mActionString;
    }

    /**
     * @return The on click listener to receive preference click
     *         events.
     */
    public @Nullable View.OnClickListener getOnPreferenceClickListener() {
        return mOnPreferenceClickListener;
    }

    /**
     * Set the on click listener to receive preference click
     * events.
     *
     * @param onPreferenceClickListener The listener to be set.
     */
    public void setOnPreferenceClickListener(
            @Nullable View.OnClickListener onPreferenceClickListener) {
        this.mOnPreferenceClickListener = onPreferenceClickListener;

        onUpdate();
    }

    /**
     * Set an action button for this preference to perform secondary
     * operations like requesting a permission, reset the preference
     * value etc. Extending preference should implement the functionality
     * in {@link #onUpdate()} method.
     *
     * @param actionString The string to be shown for the action.
     * @param onActionClickListener The on click listener to perform the action
     *                              when it is clicked.
     */
    public void setActionButton(@Nullable CharSequence actionString,
                                @Nullable OnClickListener onActionClickListener) {
        this.mActionString = actionString;
        this.mOnActionClickListener = onActionClickListener;

        onUpdate();
    }

    /**
     * @return The on click listener to receive action click events.
     */
    public @Nullable OnClickListener getOnActionClickListener() {
        return mOnActionClickListener;
    }

    /**
     * @return The listener to get various callbacks related to the
     *         popup and dialog.
     */
    public @Nullable OnPromptListener getOnPromptListener() {
        return mOnPromptListener;
    }

    /**
     * Set the listener to get various callbacks related to the
     * popup and dialog. It will be useful if this preference
     * is displaying a popup or dialog and we have to restrict
     * it from doing that.
     *
     * @param onPromptListener The listener to be set.
     */
    public void setOnPromptListener(@Nullable OnPromptListener onPromptListener) {
        this.mOnPromptListener = onPromptListener;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(mDependency)) {
            setEnabled(DynamicPreferences.getInstance().loadPrefs(key, isEnabled()));
        }
    }
}
