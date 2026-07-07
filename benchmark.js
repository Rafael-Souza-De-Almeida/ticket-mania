import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    high_traffic_spike: {
      executor: 'shared-iterations',
      vus: 500,
      iterations: 500,
      maxDuration: '10s',
    },
  },
};

export function setup() {
  const loginPayload = JSON.stringify({
    email: 'admin@ticketmania.com',
    password: 'admin123'
  });

  const loginRes = http.post('http://localhost:8080/api/v1/auth/login', loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });


  const targetTicketId = '845d6231-8d4d-41c3-b859-36ab93a89dfa';

  return {
    token: loginRes.json('token'),
    ticketId: targetTicketId
  };
}

export default function (data) {
  const url = 'http://localhost:8080/api/v1/orders';
  const payload = JSON.stringify({ ticketId: data.ticketId });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${data.token}`,
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'Successful Buyer (HTTP 200/201)': (r) => r.status === 200 || r.status === 201,
    'Blocked by Redis Lock (HTTP 400/409)': (r) => r.status === 400 || r.status === 409,
  });
}