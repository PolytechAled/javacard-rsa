# BritaCard
## Architecture
### Fonction helloworld 
Dans un premier temps, pour écrire notre première instruction, nous nous sommes basés sur la commande HelloWord du premier TP. Cela nous a permit, au départ, de tester l'envoie et la réception d'APDU via scriptor.
### PIN
### RSA encryption
### Instruction
| `INS` | Description                             | Authentification| Entrée           | Sortie |
|-------|-----------------------------------------|-----------------|------------------|-------------------------|
| 01  | Renvoie un Helloworld                   |                 | Message          |                         |
| 02  | Fonction de login                       |       Oui       | PIN (4B)         |                         |
| 03  | Renvoie la clé publique                 |                 |                  | clé publique            |
| 04  | Signe un message                        |       Oui       | Message (3B)     | Signature (64B)         |


## Tester l'applet
Avant de devélopper notre client en **bash**, nous avons testé notre applet avec le binaire scriptor qui permet d'envoyer et recevoir des APDU facilement.
Exemple de script:
```
00 A4 04 00 0B A0 00 00 00 62 03 01 0C 06 01 02 -> Selection de l'applet
10 01 00 00 00 -> INS HelloWorld
10 01 00 00 08 -> Retour d'HelloWord avec un Le = 8 Bytes
```

## Client
Nous avons pris le parti de faire notre client directement en bash, avec en tête l'utilisation de scriptor, openssl etc... pour vérifier le fonctionnement et la signature d'un message par notre applet.
Notre client se compose d'un shell intéractif et pour connaitre les intéractions possibles, vous pouvez entrée "help" (ou n'importe quoi d'autres) pour afficher l'aide.
### PIN
Scriptor n'étant pas intéractif, nous avons du créer un fichier d'instruction "template" permettant lors de la saisie du PIN par l'utilisateur, de remplacer cette valeur dans le fichier.
Template:
```
00 A4 04 00 0B A0 00 00 00 62 03 01 0C 06 01 02
10 01 00 00 00
10 01 00 00 08
10 02 00 00 04 PIN
```
Avec remplacement du PIN:
```
00 A4 04 00 0B A0 00 00 00 62 03 01 0C 06 01 02
10 01 00 00 00
10 01 00 00 08
10 02 00 00 04 09 08 06 06
```
