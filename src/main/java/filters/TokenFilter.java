package filters;

import com.google.inject.Inject;

import dao.TokenDao;
import models.Token;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;

public class TokenFilter implements Filter {
  private static final String TOKEN_HEADER = "X-AUTH-TOKEN";

  @Inject
  TokenDao tokenDao;

  @Override
  public Result filter(FilterChain chain, Context context) {
    Token token = new Token();
    token.setToken(context.getHeader(TOKEN_HEADER));

    if (tokenDao.isValidToken(token)) {
      try {
        return chain.next(context);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    } else {
      return Results.forbidden().json();
    }
  }

}
