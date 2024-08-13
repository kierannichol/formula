namespace FormulaTest;

using Formula;
using static ResolvedValueAssertions;

public class FormulaTest
{
    [Test, TestCaseSource(typeof(FormulaTestCaseSource), nameof(FormulaTestCaseSource.GetTestCases))]
    public void DataDrivenTestCases(FormulaTestCaseSource.Case data)
    {
        try
        {
            var resolved = Formula.Parse(data.Formula);
            var value = resolved.Resolve(data.Data ?? DataContext.Empty);

            if (data.ExpectedName != null)
            {
                AssertResolvedValue(value).HasName(data.ExpectedName);
            }

            if (data.ExpectedText != null)
            {
                AssertResolvedValue(value).HasValue(data.ExpectedText);
            }

            if (data.ExpectedNumber.HasValue)
            {
                AssertResolvedValue(value).HasValue(data.ExpectedNumber.Value);
            }

            if (data.ExpectedBoolean.HasValue)
            {
                AssertResolvedValue(value).HasValue(data.ExpectedBoolean.Value);
            }
        }
        catch (Exception e)
        {
            if (data.ExpectedError == null)
            {
                throw;
            }
            Assert.That(e.Message, Is.EqualTo(data.ExpectedError));
        }
    }
}