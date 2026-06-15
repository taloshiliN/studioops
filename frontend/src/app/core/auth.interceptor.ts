import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if(!request.url.startsWith('/api') ||
    request.headers.has('Authorization')){
    return next(request);
  }

  const authorization = inject(AuthService).getAuthorization();

  if (!authorization){
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: authorization
      }
    })
  );
};
