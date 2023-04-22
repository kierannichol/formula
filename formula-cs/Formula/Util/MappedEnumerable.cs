using System.Collections;

namespace Formula.Util;

public class MappedEnumerable<T, TResult> : IEnumerable<TResult>
{
    private readonly IEnumerable<T> _enumerable;
    private readonly Func<T, TResult> _mappingFunction;

    public MappedEnumerable(IEnumerable<T> enumerable, Func<T, TResult> mappingFunction)
    {
        _enumerable = enumerable;
        _mappingFunction = mappingFunction;
    }

    public IEnumerator<TResult> GetEnumerator()
    {
        throw new NotImplementedException();
    }

    IEnumerator IEnumerable.GetEnumerator()
    {
        return GetEnumerator();
    }
}

public class MappedEnumerator<T, TResult> : IEnumerator<TResult>
{
    private readonly IEnumerator<T> _enumerator;
    private readonly Func<T, TResult> _mappingFunction;
    private bool _hasCachedCurrent;
    private TResult? _cachedCurrent;

    public MappedEnumerator(IEnumerator<T> enumerator, Func<T, TResult> mappingFunction)
    {
        _enumerator = enumerator;
        _mappingFunction = mappingFunction;
    }

    public bool MoveNext()
    {
        try
        {
            return _enumerator.MoveNext();
        }
        finally
        {
            _hasCachedCurrent = false;
        }
    }

    public void Reset()
    {
        _enumerator.Reset();
        _hasCachedCurrent = false;
    }

    public TResult Current
    {
        get
        {
            if (_hasCachedCurrent && _cachedCurrent != null)
            {
                return _cachedCurrent;
            }

            _cachedCurrent = _mappingFunction(_enumerator.Current);
            _hasCachedCurrent = true;
            return _cachedCurrent;
        }
    }

    object IEnumerator.Current => Current;

    public void Dispose()
    {
        _enumerator.Dispose();
    }
}