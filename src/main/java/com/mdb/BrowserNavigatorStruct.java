package com.mdb;

public class BrowserNavigatorStruct {

    public int count;
    public int cursor;
    public int shift;
    public int before;
    public int after;
    public int totalSize;
    public int index;

    public int viewSize = 0;
    public int viewPosStart = 0;
    public int viewPosEnd = -1;

    public int beforePosStart = 0;
    public int beforePosEnd = -1;

    public int afterPosStart = 0;
    public int afterPosEnd = -1;

    public BrowserNavigatorStruct(int count, int cursor, int shift, int before, int after, int totalSize, int index) {
        this.count = count;
        this.cursor = cursor;
        this.shift = shift;
        this.before = before;
        this.after = after;
        this.totalSize = totalSize;
        this.index = index;
    }

    public void calculatePositions() throws Exception {
        if (count < 1) {
            throw new Exception("Invalid browser request: count");
        }
        disableSections();
        if (totalSize < 0) {
            totalSize = 0;
            viewSize = 0;
            count = 0;
            return;
        }
        if (index < 0) {
            index = -index - 1;
        }
        index = checkBoundaries(index);
        int base = checkBoundaries(index + (long) shift);

        if (totalSize < count) {
            viewPosStart = 1;
            viewPosEnd = totalSize;
            cursor = base - 1;
            count = totalSize;
            return;
        }
        viewPosStart = checkBoundaries(base - cursor);
        viewPosEnd = checkBoundaries(viewPosStart + count - 1);
        if (viewPosEnd == totalSize) {
            viewPosStart = viewPosEnd - count + 1;
        }
        cursor = index - viewPosStart;
        count = viewPosEnd - viewPosStart + 1;

        viewSize = count;
        if (viewPosEnd < totalSize) {
            afterPosStart = viewPosEnd + 1;
            afterPosEnd = checkBoundaries(afterPosStart + after - 1);
        }
        if (viewPosStart > 1) {
            beforePosEnd = viewPosStart - 1;
            beforePosStart = checkBoundaries(beforePosEnd - after + 1);
        }
    }

    private void disableSections() {
        viewPosStart = 0;
        viewPosEnd = -1;
        beforePosStart = 0;
        beforePosEnd = -1;
        afterPosStart = 0;
        afterPosEnd = -1;
    }

    private int checkBoundaries(long position) {
        if (position > totalSize) {
            return totalSize;
        } else if (position < 1) {
            return 1;
        } else {
            return (int) position;
        }
    }

    public void calculatePositionsOld() throws Exception {

        if (count < 1) {
            throw new Exception("Invalid browser request: count");
        }

        if (cursor < 0) {
            cursor = 0;
        } else if (cursor >= count) {
            cursor = count - 1;
        }
        if (totalSize < 0) {
            totalSize = 0;
        }

        int beforeCsr = cursor;
        int afterCsr = count - cursor - 1;
        viewSize = count;

        if (index < 0) {
            index = -index - 1;
        }
        if (index > totalSize) {
            index = totalSize;
        } else if (index < 1) {
            index = 1;
        }

        if (totalSize < viewSize) {
            viewPosStart = 1;
            viewPosEnd = totalSize;
            cursor = index - 1;
        } else {
            viewPosStart = index - beforeCsr;
            viewPosEnd = index + afterCsr;
            if (viewPosStart < 1) {
                int correction = 1 - viewPosStart;
                viewPosStart += correction;
                viewPosEnd += correction;
            } else if (viewPosEnd > totalSize) {
                int correction = viewPosEnd - totalSize;
                viewPosStart -= correction;
                viewPosEnd -= correction;
            }
            cursor = index - viewPosStart;
        }
        viewSize = viewPosEnd - viewPosStart + 1;

        viewPosStart += shift;
        viewPosEnd += shift;
        index += shift;
        if (viewPosStart < 1) {
            int correction = 1 - viewPosStart;
            viewPosStart += correction;
            viewPosEnd += correction;
            index += correction;
        } else if (viewPosEnd > totalSize) {
            int correction = viewPosEnd - totalSize;
            viewPosStart -= correction;
            viewPosEnd -= correction;
            index -= correction;
        }

        afterPosStart = viewPosEnd + 1;
        afterPosEnd = afterPosStart > totalSize ? totalSize : afterPosStart + after - 1;
        if (afterPosEnd > totalSize) {
            afterPosEnd = totalSize;
        }

        beforePosEnd = viewPosStart - 1;
        beforePosStart = beforePosEnd < 1 ? 0 : beforePosEnd - before + 1;
        if (beforePosStart < 1) {
            beforePosStart = 1;
        }

    }

}
