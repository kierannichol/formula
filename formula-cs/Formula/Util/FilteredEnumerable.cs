using System.Collections;

namespace Formula.Util;

public class FilteredEnumerable<T> : IEnumerable<T>
{
    private readonly IEnumerable<T> _enumerable;
    private readonly Predicate<T> _predicate;

    public FilteredEnumerable(IEnumerable<T> enumerable, Predicate<T> predicate)
    {
        _enumerable = enumerable;
        _predicate = predicate;
    }

    public IEnumerator<T> GetEnumerator()
    {
        return new FilteredEnumerator<T>(_enumerable.GetEnumerator(), _predicate);
    }

    IEnumerator IEnumerable.GetEnumerator()
    {
        return GetEnumerator();
    }
}

public class FilteredEnumerator<T> : IEnumerator<T>
{
    private readonly IEnumerator<T> _enumerator;
    private readonly Predicate<T> _predicate;

    public FilteredEnumerator(IEnumerator<T> enumerator, Predicate<T> predicate)
    {
        _enumerator = enumerator;
        _predicate = predicate;
    }

    public bool MoveNext()
    {
        var result = _enumerator.MoveNext();
        while (result && !_predicate(_enumerator.Current))
        {
            result = _enumerator.MoveNext();
        }
        return result;
    }

    public void Reset()
    {
        _enumerator.Reset();
        if (!_predicate(_enumerator.Current))
        {
            MoveNext();
        }
    }

    object IEnumerator.Current => Current;

    public T Current => _enumerator.Current;

    public void Dispose()
    {
        _enumerator.Dispose();
    }
}