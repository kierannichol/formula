namespace Formula.TokenTree;

public class TokenTree<T>
{
    private readonly RootNode<T> _root = new RootNode<T>();

    public static TokenTree<T> Create() {
        return new TokenTree<T>();
    }

    public TokenTree<T> IgnoreWhitespaces()
    {
        return Add(NodeExpression.AnyOf(CharacterClass.BlankCharacters), 
            _ => default);
    }

    public TokenTree<T> Add(IEnumerable<char> allowed, TokenMapper<T> mapper) {
        return AddBranch(
            NodeExpression.Term(allowed),
            mapper);
    }

    public TokenTree<T> Add(INodeExpression expression, TokenMapper<T> mapper) {
        return AddBranch(
                expression,
                mapper);
    }

    public TokenTree<T> Add(IEnumerable<INodeExpression> expressions, TokenMapper<T> mapper) {
        return AddBranch(
                CompositeNodeExpression.Of(expressions),
                mapper);
    }

    public List<T> Parse(string text) {
        var tokens = new List<T>();
        for (var i = 0; i < text.Length; i++)
        {
            var matches = new List<TokenMatch<T>>();
            _root.Walk(text, i, matches);
            if (matches.Count > 0) 
            {
                var match = matches[0];
                var token = match.Get();
                if (token != null) 
                {
                    tokens.Add(token);
                }
                i = match.EndIndex - 1;
            } 
            else 
            {
                throw new ParseException(
                        GenerateParseErrorMessage(i, text, "did not expect character: '" + text[i] + "'"),
                        text, i);
            }
        }
        return tokens;
    }

    private TokenTree<T> AddBranch(INodeExpression expression, TokenMapper<T> mapper) {
        var node = expression.ChainTo(_root);
        var matcherNode = (MatcherNode<T>) node;
        if (matcherNode.Mapper != null) {
            throw new Exception("Conflicting tokens");
        }

        matcherNode.Mapper = mapper;
        return this;
    }

    private static string GenerateParseErrorMessage(int index, String text, String message) {
        return $"Parse error at index {index} of \"{text}\": {message}" + "\n"
                                                                        + text
                                                                        + "\n"
                                                                        + "^".PadLeft(index);
    }
}