import { HttpInterceptorFn } from '@angular/common/http';
import {environment} from '../../environments/environment.development';


export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem(environment.TOKEN_NAME);

  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    return next(cloned);
  }

  return next(req);
};
