# Cart Module

Module de gestion du panier d'achat pour les billets d'événements.

## Vue d'ensemble

Le module Cart permet aux utilisateurs de :
- Ajouter des billets d'événements à leur panier avec une catégorie de prix spécifique
- Mettre à jour la quantité des articles
- Supprimer des articles individuels
- Vider complètement leur panier
- Consulter leur panier avec le prix total
- Obtenir le nombre d'articles dans leur panier

## Architecture

### Entités

#### Cart
Représente le panier d'un utilisateur.
- Un utilisateur ne peut avoir qu'un seul panier ACTIF à la fois
- Le total est calculé automatiquement
- Les articles sont gérés via une relation `@OneToMany`

#### CartItem
Représente un article dans le panier.
- Stocke des snapshots des données de l'événement (nom, prix) pour préserver l'intégrité
- Le prix total est calculé automatiquement (quantité × prix unitaire)
- Ne peut pas exister sans un Cart parent (orphanRemoval = true)

### Statuts de panier

```java
public enum CartStatusEnum {
    ACTIVE,      // Panier actif en cours
    ORDERED,     // Converti en commande
    ABANDONED    // Abandonné par l'utilisateur
}
```

## API Endpoints

Tous les endpoints nécessitent une authentification JWT.

### Ajouter un article

```http
POST /api/v1/cart/items
Content-Type: application/json
Authorization: Bearer {token}

{
  "eventId": "uuid-de-l-evenement",
  "priceCategoryId": "uuid-de-la-categorie-prix",
  "quantity": 2
}
```

**Règles métier** :
- Valide que l'événement existe
- Valide que la catégorie de prix appartient à cet événement
- Si l'article existe déjà (même événement + catégorie), la quantité est augmentée
- Sinon, crée un nouvel article avec snapshot du prix actuel

### Obtenir mon panier

```http
GET /api/v1/cart
Authorization: Bearer {token}
```

**Réponse** :
```json
{
  "isSuccess": true,
  "value": {
    "id": "cart-uuid",
    "userId": "user-uuid",
    "totalPrice": 150.00,
    "eventDtos": [
      {
        "id": "item-uuid",
        "eventId": "event-uuid",
        "eventName": "Concert Rock",
        "priceCategoryId": "category-uuid",
        "priceCategoryName": "VIP",
        "quantity": 2,
        "unitPrice": 75.00,
        "totalPrice": 150.00
      }
    ]
  }
}
```

### Mettre à jour la quantité

```http
PUT /api/v1/cart/items
Content-Type: application/json
Authorization: Bearer {token}

{
  "cartItemId": "item-uuid",
  "quantity": 5
}
```

### Supprimer un article

```http
DELETE /api/v1/cart/items/{cartItemId}
Authorization: Bearer {token}
```

### Vider le panier

```http
DELETE /api/v1/cart
Authorization: Bearer {token}
```

### Obtenir le nombre d'articles

```http
GET /api/v1/cart/count
Authorization: Bearer {token}
```

**Réponse** :
```json
{
  "isSuccess": true,
  "value": 3
}
```

## Utilisation dans le Code

### Service Layer

```java
@Autowired
private ICartService cartService;

// Ajouter au panier
AddToCartDto dto = new AddToCartDto(eventId, priceCategoryId, 2);
cartService.addToCart(dto);

// Obtenir le panier
GetCartDto cart = cartService.getMyCart();

// Mettre à jour un article
UpdateCartItemDto updateDto = new UpdateCartItemDto(cartItemId, 5);
cartService.updateCartItem(updateDto);

// Supprimer un article
cartService.removeFromCart(cartItemId);

// Vider le panier
cartService.clearCart();

// Compter les articles
Integer count = cartService.getCartItemsCount();
```

### Repository Layer

```java
@Autowired
private ICartRepository cartRepository;

// Trouver le panier actif d'un utilisateur
Optional<Cart> cart = cartRepository.findByUserIdAndStatus(userId, CartStatusEnum.ACTIVE);

// Trouver un panier par utilisateur
Optional<Cart> cart = cartRepository.findByUserId(userId);
```

## Exceptions

### CartNotFoundException
Levée quand le panier n'est pas trouvé.
```java
throw new CartNotFoundException(userId);
```
→ HTTP 404

### CartItemNotFoundException
Levée quand l'article du panier n'est pas trouvé.
```java
throw new CartItemNotFoundException(cartItemId);
```
→ HTTP 404

### InvalidQuantityException
Levée quand la quantité est invalide (< 1).
```java
throw new InvalidQuantityException();
```
→ HTTP 400

## Pattern Snapshot

Le module Cart utilise le **Snapshot Pattern** pour préserver l'intégrité des données :

```java
@Entity
public class CartItem {
    // Snapshots - ne changent jamais après création
    private String eventName;           // Nom de l'événement au moment de l'ajout
    private String priceCategoryName;   // Nom de la catégorie au moment de l'ajout
    private BigDecimal unitPrice;       // Prix au moment de l'ajout

    // Références - pour validation/lookup
    private UUID eventId;
    private UUID priceCategoryId;
}
```

**Avantages** :
- Le panier reste valide même si l'événement est supprimé
- Les prix ne changent pas après ajout
- Données historiques préservées

## Flux de travail typique

1. **Utilisateur parcourt les événements**
   ```
   GET /api/v1/events
   ```

2. **Utilisateur ajoute un billet au panier**
   ```
   POST /api/v1/cart/items
   {
     "eventId": "...",
     "priceCategoryId": "...",
     "quantity": 2
   }
   ```

3. **Utilisateur consulte son panier**
   ```
   GET /api/v1/cart
   ```

4. **Utilisateur modifie la quantité**
   ```
   PUT /api/v1/cart/items
   {
     "cartItemId": "...",
     "quantity": 3
   }
   ```

5. **Utilisateur procède au paiement** (module Booking)
   ```
   POST /api/v1/bookings/from-cart
   ```

6. **Le panier devient ORDERED**

## Règles métier

1. **Un panier actif par utilisateur**
   - L'utilisateur ne peut avoir qu'un seul panier avec status = ACTIVE
   - Les anciens paniers sont marqués ORDERED ou ABANDONED

2. **Fusion automatique des doublons**
   - Si l'utilisateur ajoute le même événement + catégorie
   - La quantité est augmentée au lieu de créer un doublon

3. **Validation stricte**
   - L'événement doit exister
   - La catégorie de prix doit appartenir à l'événement
   - La quantité doit être ≥ 1

4. **Calcul automatique des totaux**
   - `CartItem.totalPrice = unitPrice × quantity`
   - `Cart.total = SUM(items.totalPrice)`
   - Recalculé automatiquement via `@PreUpdate`

5. **Sécurité**
   - L'utilisateur ne peut accéder qu'à son propre panier
   - Validation de propriété pour toutes les opérations

## Intégration avec d'autres modules

### Event Module
```java
// CartService accède à IEventRepository pour :
- Valider que l'événement existe
- Récupérer les catégories de prix
- Créer des snapshots des données
```

### Booking Module (futur)
```java
// Conversion panier → réservation
Booking createFromCart(UUID cartId) {
    Cart cart = cartService.getCart();
    // Créer réservation à partir des items du panier
    cart.setStatus(CartStatusEnum.ORDERED);
}
```

### Payment Module (futur)
```java
// Paiement du panier
Payment checkoutCart(UUID cartId, PaymentMethod method) {
    Cart cart = cartService.getCart();
    // Traiter le paiement
    // Créer la commande
}
```

## Tests

### Tests Bruno
Fichiers de test API dans `http/Cart/` :
- `AddToCart.bru` - Ajouter un article
- `GetMyCart.bru` - Obtenir le panier
- `UpdateCartItem.bru` - Mettre à jour la quantité
- `RemoveFromCart.bru` - Supprimer un article
- `ClearCart.bru` - Vider le panier
- `GetCartCount.bru` - Compter les articles

### Tests architecturaux
```bash
./mvnw test -Dtest=*ArchitectureTest,*NamingTest,RepositoryAccessTest
```

## Améliorations futures

1. **Expiration des paniers**
   - Job planifié pour nettoyer les paniers abandonnés
   - Configurable via `cart.expiration.days`

2. **Validation d'inventaire**
   - Vérifier la disponibilité des billets avant ajout
   - Réserver temporairement les billets dans le panier

3. **Limites de panier**
   - Nombre maximum d'articles par panier
   - Quantité maximum par article

4. **Notifications**
   - Email de rappel pour paniers abandonnés
   - Alerte si prix a changé

5. **Analytics**
   - Taux d'abandon de panier
   - Articles populaires
   - Temps moyen avant conversion

## Dépannage

### Le panier n'est pas créé
- Vérifier que l'utilisateur est authentifié
- Vérifier que l'événement existe
- Vérifier que la catégorie de prix appartient à l'événement

### Les totaux sont incorrects
- Les totaux sont recalculés automatiquement
- Vérifier les logs pour les erreurs de calcul
- Les snapshots de prix préservent les prix historiques

### Erreur "Cart not found"
- Le panier est créé automatiquement au premier ajout
- Vérifier que l'utilisateur a bien un panier ACTIVE
- Un utilisateur peut avoir plusieurs paniers avec différents statuts

## Configuration

Aucune configuration spécifique requise. Le module utilise les configurations communes :
- `app.api-version` - Version de l'API (défaut: /api/v1)
- `spring.jpa.hibernate.ddl-auto` - Gestion du schéma (défaut: update)

## Base de données

### Tables
- `carts` - Paniers des utilisateurs
- `cart_items` - Articles dans les paniers

### Index recommandés
```sql
CREATE INDEX idx_cart_user_id ON carts(user_id);
CREATE INDEX idx_cart_user_status ON carts(user_id, status);
CREATE INDEX idx_cartitem_cart_id ON cart_items(cart_id);
```

## Support

Pour signaler un bug ou demander une fonctionnalité :
1. Vérifier les logs : `./mvnw spring-boot:run`
2. Tester avec les fichiers Bruno
3. Consulter la documentation du module Event
