import http from 'k6/http';
import { sleep } from 'k6';

const scheme = "http";
const pulceoHostname = "127.0.0.1";
const prmPort = "7878";
const pmsPort = "7777";
const pnaHostname1 = "127.0.0.1"
const pnaHostname2 = "127.0.0.2"

export default function () {
  const url = scheme + "://" + pulceoHostname + ":" + prmPort + "/api/v1/health";
  const payload = JSON.stringify({
    email: 'aaa',
    password: 'bbb',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };
  http.get(url, payload, params);
}

/* on-prem-track */

// create node
export function createNode() {
    const url = scheme + "://" + pulceoHostname + ":" + prmPort + "/api/v1/nodes";
    const payload = JSON.stringify({
        "providerName": "default ",
        "hostname": scheme + "://" + pnaHostname1 + ":" + pmsPort,
        "pnaInitToken": "b0hRUGwxT0hNYnhGbGoyQ2tlQnBGblAxOmdHUHM3MGtRRWNsZVFMSmdZclFhVUExb0VpNktGZ296"
    });
    http.post(url, payload, params);
}

// retrieve by ID




export function contacts() {

  http.get('https://test.k6.io/contacts.php', {

    tags: { my_custom_tag: 'contacts' },

  });

}