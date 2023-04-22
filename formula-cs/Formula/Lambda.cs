namespace Formula;

public interface ILambda0<out TResult>
{
    TResult Execute();
}

public interface ILambda1<in TArg1, out TResult>
{
    TResult Execute(TArg1 a1);
}

public interface ILambda2<in TArg1, in TArg2, out TResult>
{
    TResult Execute(TArg1 a1, TArg2 a2);
}

public interface ILambda3<in TArg1, in TArg2, in TArg3, out TResult>
{
    TResult Execute(TArg1 a1, TArg2 a2, TArg3 a3);
}