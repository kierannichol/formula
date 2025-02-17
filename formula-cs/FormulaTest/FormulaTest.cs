using FormulaTest.Assertions;

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
            if (resolved == null)
            {
                Assert.Fail("Resolved value was null");
                return;
            }
            
            var value = resolved.Resolve(data.Data ?? DataContext.Empty);

            if (data.ExpectedError != null)
            {
                throw new Exception("Expected error, but was none");
            }

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

            if (data.ExpectedList != null)
            {
                AssertResolvedValue(value).HasValue(data.ExpectedList);
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