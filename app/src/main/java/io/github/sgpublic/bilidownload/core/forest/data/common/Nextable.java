package io.github.sgpublic.bilidownload.core.forest.data.common;

import lombok.Data;

/**
 * @author Madray Haven
 * @date 2022/10/27 9:04
 */
@Data
public class Nextable {
    private int hasNext;

    public boolean isHasNext() {
        return getHasNext() == 1;
    }
}
