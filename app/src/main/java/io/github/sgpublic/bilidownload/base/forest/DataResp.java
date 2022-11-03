package io.github.sgpublic.bilidownload.base.forest;

import androidx.annotation.NonNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public abstract class DataResp<T> extends CommonResp<T> {
    @NonNull
    private T data;
}
