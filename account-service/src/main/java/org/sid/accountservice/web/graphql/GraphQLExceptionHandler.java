package org.sid.accountservice.web.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.sid.accountservice.exception.AccountNotFoundException;
import org.sid.accountservice.exception.BusinessException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        ErrorType type;
        if (ex instanceof AccountNotFoundException) type = ErrorType.NOT_FOUND;
        else if (ex instanceof BusinessException) type = ErrorType.BAD_REQUEST;
        else return null;
        return GraphqlErrorBuilder.newError(env).errorType(type).message(ex.getMessage()).build();
    }
}
