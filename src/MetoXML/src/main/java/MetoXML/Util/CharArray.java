package MetoXML.Util;

public class CharArray {
    private char[] array = null;

    private int length = 0;

    private int capacity = 0;

    public char[] Array() {
		return array;
	}

	public int Length() {
		return length;
	}

	public int getCapacity() {
		return capacity;
	}

	public CharArray(int capacity)
    {
        this.capacity = capacity;
        array = new char[capacity];
    }

    public void Set(int index, char chr)
    {
        if (index >= this.length)
        {
            throw new IndexOutOfBoundsException();
        }

        array[index] = chr;
    }

    public char Get(int index)
    {
        if (index >= this.length)
        {
            throw new IndexOutOfBoundsException();
        }

        return array[index];
    }

    public void Add(char chr)
    {
        array[this.length] = chr;
        this.length++;
    }

    public void Add(char[] chrArray, int offset, int len)
    { 
        if (offset <= 0) throw new IndexOutOfBoundsException();
        if ((offset + len) > chrArray.length) throw new IndexOutOfBoundsException();
        if ((len + this.length) > this.capacity) throw new IndexOutOfBoundsException();

        /*
        for(int i = offset; i < length; i++)
        {
            array[this.length++] = chrArray[i];
        }
        */
        System.arraycopy(chrArray, offset, array, this.length, len);
        this.length += len;
    }

    public void Remove(int startIndex, int length)
    {
        if (length == 0)
        {
            return;
        }

        if(startIndex < 0 || startIndex >= this.length)
        {
            throw new IndexOutOfBoundsException();
        }

        if(length < 0)
        {
            throw new IndexOutOfBoundsException();
        }

        int endIndex = startIndex + length - 1;
        if (endIndex >= this.length)
        {
            //endIndex = this._length - 1;
            throw new IndexOutOfBoundsException();
        }

        int len = endIndex - startIndex + 1;

        int index = startIndex;
        for (int i = endIndex + 1; i < this.length; i++)
        {
            array[index++] = array[i];
        }

        this.length = this.length - len;
    }

    public void Clear()
    {
        this.length = 0;
    }
}
