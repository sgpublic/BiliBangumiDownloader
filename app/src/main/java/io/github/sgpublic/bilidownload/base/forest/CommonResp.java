package io.github.sgpublic.bilidownload.base.forest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class CommonResp<T> {
    @NonNull
    private Integer code;

    @Nullable
    private String message;

    @NonNull
    public abstract T getData();
}
