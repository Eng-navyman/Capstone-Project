/*
 * MIT License
 *
 * Copyright (c) 2016 Kartik Sharma
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.crazyhitty.chdev.ks.predator.core.categories;

import android.content.Context;

import com.crazyhitty.chdev.ks.predator.core.BasePresenter;
import com.crazyhitty.chdev.ks.predator.core.BaseView;
import com.crazyhitty.chdev.ks.predator.models.Category;

import java.util.List;

/**
 * Author:      Kartik Sharma
 * Email Id:    cr42yh17m4n@gmail.com
 * Created:     2/15/2017 2:50 PM
 * Description: Unavailable
 */

public interface CategoriesContract {
    interface View extends BaseView<Presenter> {
        void showCategories(List<Category> categories);

        void categoriesAvailable();

        void categoriesUnavailable();
    }

    interface Presenter extends BasePresenter {
        void fetchCategories(Context context, String token);

        void fetchCategoriesOffline(Context context);

        void checkIfCategoriesAreAvailable();
    }
}
