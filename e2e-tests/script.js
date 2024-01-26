import http from 'k6/http';
import { sleep } from 'k6';

export default function () {
  const url = 'http://127.0.0.1:7878';
  const payload = JSON.stringify({
    email: 'aaa',
    password: 'bbb',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  http.post(url, payload, params);
}