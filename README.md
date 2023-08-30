# Project_DS_Giacomello_Vecellio
Ultimi cambiamenti 30 agosto

Cambiamenti:
- aggiunto il campo 'owner' dentro request per facilitare l'unlock di un item <br>
- creazione di UnlockMsg e rispettivo handler<br>
- creazione di OkMsg e rispettivo handler: serve in caso di update request, dopo aver mandato l'ordine di aggiornare il valore, per raccogliere gli ok dei messaggi che hanno fatto l'update del valore<br>
- creazione del campo okResponses nella classe Request<br>
<br>
Quando viene raggiunto il quorum per una update request:
- Si manda il messaggio di cambio del valore<br>
- Si attende un numero di messaggi di ok pari a write_quorum <br>
- Si comunica all'owner di fare l'unlock del messaggio<br>
<br>
Osservazioni durante il main:
- Un update funziona se è l'unica operazione che viene fatta<br>
- Se ci sono più read il problema non si pone<br>
- Se si mischiano write e read una delle due operazioni (a volte una, a volte l'altra) non viene eseguita<br>
- Non ho avuto cuore di testare più di un update<br>
<br>
Possibili cause del problema:
- la logica dei metodi di lock dentro item<br>
- la concorrenzialità<br>
