<?php
// GRC VANGUARD login endpoint for cyberpantology.in
// Validates credentials against cyberpan_GRC_vangaurd.login (username, password, email, remark)
// Deploy this file to: https://cyberpantology.in/api/grc_login.php (or update the app's Login API URL)
//
// NOTE: This is a minimal template. Use HTTPS only, store hashed passwords in production,
// and restrict DB credentials/permissions appropriately.

header('Content-Type: application/json; charset=utf-8');
header('X-Content-Type-Options: nosniff');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['ok' => false, 'error' => 'Method not allowed']);
    exit;
}

$raw = file_get_contents('php://input');
$data = json_decode($raw, true);
if (!is_array($data)) {
    // also accept form-encoded
    $data = $_POST;
}

$username = isset($data['username']) ? trim((string)$data['username']) : '';
$password = isset($data['password']) ? (string)$data['password'] : '';

if ($username === '' || $password === '') {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Username and password required']);
    exit;
}

// --- Database configuration (update for your hosting environment) ---
$db_host = 'localhost';
$db_name = 'cyberpan_GRC_vangaurd';
$db_user = 'YOUR_DB_USER';
$db_pass = 'YOUR_DB_PASSWORD';

try {
    $pdo = new PDO(
        "mysql:host=$db_host;dbname=$db_name;charset=utf8mb4",
        $db_user,
        $db_pass,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]
    );

    $stmt = $pdo->prepare('SELECT username, email, remark FROM login WHERE username = :u AND password = :p LIMIT 1');
    $stmt->execute([':u' => $username, ':p' => $password]);
    $row = $stmt->fetch();

    if ($row) {
        echo json_encode([
            'ok' => true,
            'username' => $row['username'],
            'message' => 'Signed in'
        ]);
    } else {
        http_response_code(401);
        echo json_encode(['ok' => false, 'error' => 'Invalid username or password']);
    }
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(['ok' => false, 'error' => 'Server error']);
}
?>
