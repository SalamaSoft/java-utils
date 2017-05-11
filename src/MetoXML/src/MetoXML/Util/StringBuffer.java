package MetoXML.Util;

import java.util.ArrayList;
import java.util.List;

public class StringBuffer {
    private List<CharArray> _strBuff = new ArrayList<CharArray>();

    private int minCapacity = 0;

    private int rowCursor = 0;

    private int length = 0;

    public int Length() {
		return length;
	}

	public StringBuffer(int minCapacity)
    {
        this.minCapacity = minCapacity;

        IncreaseCharArray();
    }

    private void IncreaseCharArray()
    { 
        _strBuff.add(new CharArray(this.minCapacity));
    }

    public void Append(char chr)
    {
        if (_strBuff.get(this.rowCursor).Length() != _strBuff.get(this.rowCursor).getCapacity())
        {
            _strBuff.get(this.rowCursor).Add(chr);
        }
        else
        {
            this.rowCursor++;
            if (this.rowCursor >= _strBuff.size())
            {
                IncreaseCharArray();
            }

            _strBuff.get(this.rowCursor).Add(chr);
        }

        this.length++;
    }

    public void Append(char[] chrArray, int offset, int length)
    {
        if (offset <= 0) throw new IndexOutOfBoundsException();
        if ((offset + length) > chrArray.length) throw new IndexOutOfBoundsException();

        int index = offset;
        int len = length;
        int addLen = 0;

        while (true)
        {
            if ((len + _strBuff.get(this.rowCursor).Length()) > _strBuff.get(this.rowCursor).getCapacity())
            {
                addLen = _strBuff.get(this.rowCursor).getCapacity() - _strBuff.get(this.rowCursor).Length();

                _strBuff.get(this.rowCursor).Add(chrArray, index, addLen);

                len -= addLen;
                index += addLen;

                this.rowCursor++;
                if (this.rowCursor >= _strBuff.size())
                {
                    IncreaseCharArray();
                }
            }
            else
            {
                addLen = len;
                _strBuff.get(this.rowCursor).Add(chrArray, index, addLen);
                break;
            }
        }

        this.length += length;
    }

    public void Clear()
    {
        for (int i = 0; i < _strBuff.size(); i++)
        {
            _strBuff.get(i).Clear();
        }
        this.length = 0;
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

        int len = 0;
        int prevLen = 0;

        int startIndexInRow = 0;

        int startRow = -1;
        int endRow = -1;
        int currentRowLen = 0;

        for (int i = 0; i < _strBuff.size(); i++)
        {
        	currentRowLen = _strBuff.get(i).Length();
            len += currentRowLen;
            if (startRow < 0)
            {
                if (startIndex < len)
                {
                    startRow = i;
                }
            }
            if (endRow < 0)
            {
                if (endIndex < len)
                {
                    endRow = i;
                }
            }

            if (startRow >= 0)
            {
                if (endRow < 0)
                {
                    if (i == startRow)
                    {
                        //first row, and != last row
                        startIndexInRow = startIndex - prevLen;
                        _strBuff.get(i).Remove(startIndexInRow, currentRowLen - startIndexInRow);
                    }
                    else
                    {
                        //not first row, neither last row
                        _strBuff.get(i).Clear();
                    }
                }
                else
                {
                    if (startRow == endRow)
                    {
                        //first row = lastrow
                        startIndexInRow = startIndex - prevLen;
                        _strBuff.get(i).Remove(startIndexInRow, endIndex - startIndex + 1);
                    }
                    else
                    {
                        //last row, and != first row
                        _strBuff.get(i).Remove(0, endIndex - prevLen + 1);
                    }

                    break;
                }
            }

            prevLen += currentRowLen;
        }

        this.length -= length;
    }

    public int IndexOf(char chr)
    {
        int index = -1;
        int i = 0;
        int j = 0;

        for (i = 0; i < _strBuff.size(); i++)
        {
            for (j = 0; j < _strBuff.get(i).Length(); j++)
            {
                index++;

                if (_strBuff.get(i).Array()[j] == chr)
                {
                    return index;
                }
            }
        }

        return -1;
    }

    public int IndexOfAny(char[] chrArray)
    {
        int index = -1;

        int i = 0;
        int j = 0;
        int k = 0;

        for (i = 0; i < _strBuff.size(); i++)
        {
            for (j = 0; j < _strBuff.get(i).Length(); j++)
            {
                index++;

                for (k = 0; k < 0; k++)
                {
                    if (_strBuff.get(i).Array()[j] == chrArray[k])
                    {
                        return index;
                    }
                }
            }
        }

        return index;
    }

    public int IndexOf(String str)
    {
        int index = -1;
        int i = 0;
        int j = 0;
        int m = 0;
        int strLen = str.length();
        boolean isMatched = false;

        for (i = 0; i < _strBuff.size(); i++)
        {
            for (j = 0; j < _strBuff.get(i).Length(); j++)
            {
                index++;

                isMatched = true;
                for (m = 0; m < strLen; m++)
                {
                    if (GetCharAfterPosition(i, j, m) != str.charAt(m))
                    {
                        isMatched = false;
                    }
                }

                if (isMatched)
                {
                    return index;
                }
            }
        }

        return -1;
    }

    public int IndexOfAny(String[] strArray)
    {
        int index = -1;
        int i = 0;
        int j = 0;
        int m = 0;
        int k = 0;
        boolean isMatched = false;

        for (i = 0; i < _strBuff.size(); i++)
        {
            for (j = 0; j < _strBuff.get(i).Length(); j++)
            {
                index++;

                for (k = 0; k < strArray.length; k++)
                {
                    isMatched = true;
                    for (m = 0; m < strArray[k].length(); m++)
                    {
                        if (GetCharAfterPosition(i, j, m) != strArray[k].charAt(m))
                        {
                            isMatched = false;
                        }
                    }

                    if (isMatched)
                    {
                        return index;
                    }
                }
            }
        }

        return -1;
    }

    private char GetCharAfterPosition(int rowPosition, int colPosition, int offsetAfterPosition)
    {
        int colIndex = colPosition;
        int offset = offsetAfterPosition;
        int index = 0;

        for (int i = rowPosition; i < _strBuff.size(); i++)
        {
            index = colIndex + offset;
            if (index < _strBuff.get(i).Length())
            {
                return _strBuff.get(index).Array()[index];
            }
            else
            {
                colIndex = 0;
                offset -= _strBuff.get(i).Length() - colIndex;
            }
        }

        return '\0';
    }

    public String SubString(int startIndex, int length)
    {
        StringBuilder sb = new StringBuilder();

        int index = 0;
        int i = 0;
        int j = 0;

        for (i = 0; i < _strBuff.size(); i++)
        {
            for (j = 0; j < _strBuff.get(i).Length(); j++)
            {
                if (index >= startIndex)
                {
                    if (index < (startIndex + length))
                    {
                        sb.append(_strBuff.get(i).Array()[j]);
                    }
                    else
                    {
                        break;
                    }
                }

                index++;
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        //return base.ToString();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < _strBuff.size(); i++)
        {
            sb.append(_strBuff.get(i).Array(), 0, _strBuff.get(i).Length());
        }

        return sb.toString();
    }
    
    public boolean StartsWith(String str)
    {
        if (str.length() == 0) return false;
        if(this.length < str.length()) return false;

        int index = 0;
        int i = 0;
        int j = 0;

        for (i = 0; i < _strBuff.size(); i++)
        {
            for (j = 0; j < _strBuff.get(i).Length(); j++)
            {
                if (index >= str.length()) break;

                if (_strBuff.get(i).Array()[j] != str.charAt(index))
                {
                    return false;
                }

                index++;
            }
        }

        return true;
    }

    public boolean EndsWith(String str)
    {
        if (str.length() == 0) return false;
        if (this.length < str.length()) return false;

        int index = 0;
        int i = 0;
        int j = 0;

        for (i = _strBuff.size() -1; i >= 0; i--)
        {
            for (j = _strBuff.get(i).Length() - 1; j >= 0; j--)
            {
                if (index >= str.length()) break;

                if (_strBuff.get(i).Array()[j] != str.charAt(str.length() - 1 - index))
                {
                    return false;
                }

                index++;
            }
        }

        return true;
    }
}
