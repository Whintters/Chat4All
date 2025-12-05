import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 10 }, // Sobe para 10 usuários
    { duration: '30s', target: 50 }, // Mantém 50 usuários (Carga)
    { duration: '10s', target: 0 },  // Desce para 0
  ],
};

export default function () {
  const url = 'http://localhost:8080/v1/messages';
  const isWhatsapp = Math.random() > 0.5;
  const randomChatId = 'chat-load-' + Math.floor(Math.random() * 100);
  const targetRecipient = isWhatsapp ? 'wa_destinatario_carga' : 'ig_destinatario_carga';
  const payload = JSON.stringify({
    conversationId: randomChatId,
    senderId: 'wa_user_load',
    recipientId: targetRecipient,
    content: 'Teste de Carga k6 - Mensagem de alto throughput',
    type: 'text'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer chat4all-secret-key',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 202': (r) => r.status === 202 || r.status === 200,
  });

  sleep(0.1); // Pausa curta de 100ms entre envios
}